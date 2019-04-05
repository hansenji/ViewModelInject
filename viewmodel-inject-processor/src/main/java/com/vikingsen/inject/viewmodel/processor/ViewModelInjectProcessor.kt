package com.vikingsen.inject.viewmodel.processor

import com.google.auto.service.AutoService
import com.squareup.inject.assisted.processor.AssistedInjection
import com.squareup.inject.assisted.processor.Key
import com.squareup.inject.assisted.processor.NamedKey
import com.squareup.inject.assisted.processor.asDependencyRequest
import com.squareup.inject.assisted.processor.internal.MirrorValue
import com.squareup.inject.assisted.processor.internal.applyEach
import com.squareup.inject.assisted.processor.internal.associateWithNotNull
import com.squareup.inject.assisted.processor.internal.cast
import com.squareup.inject.assisted.processor.internal.castEach
import com.squareup.inject.assisted.processor.internal.findElementsAnnotatedWith
import com.squareup.inject.assisted.processor.internal.getAnnotation
import com.squareup.inject.assisted.processor.internal.getValue
import com.squareup.inject.assisted.processor.internal.hasAnnotation
import com.squareup.inject.assisted.processor.internal.toClassName
import com.squareup.inject.assisted.processor.internal.toTypeName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.ParameterizedTypeName
import com.vikingsen.inject.viewmodel.ViewModelInject
import com.vikingsen.inject.viewmodel.ViewModelModule
import com.vikingsen.inject.viewmodel.processor.internal.createGeneratedAnnotation
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind.CLASS
import javax.lang.model.element.ElementKind.CONSTRUCTOR
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.Modifier.STATIC
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic.Kind.ERROR
import javax.tools.Diagnostic.Kind.WARNING

@AutoService(Processor::class)
class ViewModelInjectProcessor : AbstractProcessor() {

    private lateinit var messager: Messager
    private lateinit var filer: Filer
    private lateinit var types: Types
    private lateinit var elements: Elements
    private lateinit var viewModelType: TypeMirror
    private var savedStateHandle: TypeMirror? = null

    private var userModule: String? = null

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()
    override fun getSupportedAnnotationTypes() = setOf(
        ViewModelInject::class.java.canonicalName,
        ViewModelModule::class.java.canonicalName
    )

    override fun init(env: ProcessingEnvironment) {
        super.init(env)
        messager = env.messager
        filer = env.filer
        types = env.typeUtils
        elements = env.elementUtils
        viewModelType = elements.getTypeElement("androidx.lifecycle.ViewModel").asType()
        savedStateHandle = elements.getTypeElement("androidx.lifecycle.SavedStateHandle")?.asType()
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val viewModelInjectElements = roundEnv.findViewModelInjectCandidateTypeElements()
            .mapNotNull { it.toViewModelInjectElementsOrNull() }

        viewModelInjectElements
            .associateWithNotNull { it.toAssistedInjectionOrNull() }
            .forEach { writeViewModelInject(it.key, it.value) }

        val viewModelModuleElements = roundEnv.findViewModelModuleTypeElement()
            ?.toViewModelModuleElementsOrNull(viewModelInjectElements)

        if (viewModelModuleElements != null) {
            val moduleType = viewModelModuleElements.moduleType

            val userModuleFqcn = userModule
            if (userModuleFqcn != null) {
                val userModuleType = elements.getTypeElement(userModuleFqcn)
                error("Multiple @ViewModelModule-annotated modules found.", userModuleType)
                error("Multiple @ViewModelModule-annotated modules found.", moduleType)
                userModule = null
            } else {
                userModule = moduleType.qualifiedName.toString()

                val viewModelInjectionModule = viewModelModuleElements.toViewModelInjectionModule()
                writeViewModelModule(viewModelModuleElements, viewModelInjectionModule)
            }
        }

        // Wait until processing is ending to validate that the @ViewModelModule's @Module annotation
        // includes the generated type.
        if (roundEnv.processingOver()) {
            val userModuleFqcn = userModule
            if (userModuleFqcn != null) {
                // In the processing round in which we handle the @InflationModule the @Module annotation's
                // includes contain an <error> type because we haven't generated the inflation module yet.
                // As a result, we need to re-lookup the element so that its referenced types are available.
                val userModule = elements.getTypeElement(userModuleFqcn)

                // Previous validation guarantees this annotation is present.
                val moduleAnnotation = userModule.getAnnotation("dagger.Module")
                // Dagger guarantees this property is present and is an array of types or errors.
                val includes = moduleAnnotation?.getValue("includes", elements)
                    ?.cast<MirrorValue.Array>()
                    ?.filterIsInstance<MirrorValue.Type>() ?: emptyList()

                val generatedModuleName = userModule.toClassName().viewModelInjectModuleName()
                val referencesGeneratedModule = includes
                    .map { it.toTypeName() }
                    .any { it == generatedModuleName }
                if (!referencesGeneratedModule) {
                    error(
                        "@ViewModelModule's @Module must include ${generatedModuleName.simpleName()}",
                        userModule
                    )
                }
            }
        }

        return false
    }

    /**
     * Find [TypeElement]s which are candidates for assisted injection by having a constructor
     * annotated with [ViewModelInject].
     */
    private fun RoundEnvironment.findViewModelInjectCandidateTypeElements(): List<TypeElement> {
        return findElementsAnnotatedWith<ViewModelInject>()
            .map { it.enclosingElement as TypeElement }
    }

    /**
     * From this [TypeElement] which is a candidate for view model injection, find and validate the
     * syntactical elements required to generate the factory:
     * - Non-private, non-inner target type
     * - Single non-private target constructor
     */
    private fun TypeElement.toViewModelInjectElementsOrNull(): ViewModelInjectElements? {
        var valid = true
        if (PRIVATE in modifiers) {
            error("@ViewModelInject-using types must not be private", this)
            valid = false
        }
        if (enclosingElement.kind == CLASS && STATIC !in modifiers) {
            error("Nested @ViewModelInject-using types must be static", this)
            valid = false
        }
        if (!types.isSubtype(asType(), viewModelType)) {
            error("@ViewModelInject-using types must be subtypes of ViewModel", this)
            valid = false
        }

        val constructors = enclosedElements
            .filter { it.kind == CONSTRUCTOR }
            .filter { it.hasAnnotation<ViewModelInject>() }
            .castEach<ExecutableElement>()
        if (constructors.size > 1) {
            error("Multiple @ViewModelInject-annotated constructors found.", this)
            valid = false
        }

        if (!valid) return null

        val constructor = constructors.single()
        if (PRIVATE in constructor.modifiers) {
            error("@ViewModelInject constructor must not be private.", constructor)
            return null
        }

        return ViewModelInjectElements(this, constructor)
    }

    /**
     * From this [ViewModelInjectElements], parse and validate the semantic information of the
     * elements which is required to generate the factory:
     * - Optional unqualified assisted parameter of SavedStateHandle
     * - At least one provided parameter and no duplicates
     */
    private fun ViewModelInjectElements.toAssistedInjectionOrNull(): AssistedInjection? {
        var valid = true

        val requests = targetConstructor.parameters.map { it.asDependencyRequest() }
        val (assistedRequests, providedRequests) = requests.partition { it.isAssisted }
        val assistedKeys = assistedRequests.map { it.namedKey }
        if (assistedKeys.isNotEmpty() && assistedKeys.toSet() != SAVED_STATE_FACTORY_KEY.toSet()) {
            error(
                """
        ViewModel injection only allows up to 1 @Assisted parameter of type SavedStateHandle.
          Found:
            $assistedKeys
          Expected:
            $SAVED_STATE_FACTORY_KEY
        """.trimIndent(), targetConstructor
            )
            valid = false
        } else if (assistedKeys.isNotEmpty() && savedStateHandle == null) {
            error("SavedStateHandle is missing from the classpath")
            valid = false
        }
        if (providedRequests.isEmpty()) {
            warn("ViewModel injections requires at least one non-@Assisted parameter.", targetConstructor)
        } else {
            val providedDuplicates = providedRequests.groupBy { it.key }.filterValues { it.size > 1 }
            if (providedDuplicates.isNotEmpty()) {
                error(
                    "Duplicate non-@Assisted parameters declared. Forget a qualifier annotation?"
                            + providedDuplicates.values.flatten().joinToString("\n * ", prefix = "\n * "),
                    targetConstructor
                )
                valid = false
            }
        }

        if (!valid) return null

        val targetType = targetType.asType().toTypeName()
        val generatedAnnotation = createGeneratedAnnotation(elements, "https://github.com/hansenji/ViewModelInject")
        return if (assistedKeys.isEmpty()) {
            val factory = ParameterizedTypeName.get(BASIC_FACTORY, targetType)
            AssistedInjection(targetType, requests, factory, "create", targetType, emptyList(), generatedAnnotation)
        } else {
            val factory = ParameterizedTypeName.get(SAVED_STATE_FACTORY, targetType)
            AssistedInjection(targetType, requests, factory, "create", targetType, SAVED_STATE_FACTORY_KEY, generatedAnnotation)
        }
    }

    private fun writeViewModelInject(elements: ViewModelInjectElements, injection: AssistedInjection) {
        val generatedTypeSpec = injection.brewJava()
            .toBuilder()
            .addOriginatingElement(elements.targetType)
            .build()
        JavaFile.builder(injection.generatedType.packageName(), generatedTypeSpec)
            .addFileComment("Generated by @ViewModelInject. Do not modify!")
            .build()
            .writeTo(filer)
    }

    private fun RoundEnvironment.findViewModelModuleTypeElement(): TypeElement? {
        val viewModelModules = findElementsAnnotatedWith<ViewModelModule>().castEach<TypeElement>()
        if (viewModelModules.size > 1) {
            viewModelModules.forEach {
                error("Multiple @ViewModelModule-annotated modules found.", it)
            }
            return null
        }
        return viewModelModules.singleOrNull()
    }

    private fun TypeElement.toViewModelModuleElementsOrNull(
        viewModelInjectElements: List<ViewModelInjectElements>
    ): ViewModelModuleElements? {
        if (!hasAnnotation("dagger.Module")) {
            error("@ViewModelModule must also be annotated as a Dagger @Module", this)
            return null
        }

        val viewModelTargetTypes = viewModelInjectElements.map { it.targetType }
        return ViewModelModuleElements(this, viewModelTargetTypes)
    }

    private fun ViewModelModuleElements.toViewModelInjectionModule(): ViewModelInjectionModule {
        val moduleName = moduleType.toClassName()
        val inflationNames = viewModelTypes.map { it.toClassName() }
        val public = Modifier.PUBLIC in moduleType.modifiers
        val generatedAnnotation = createGeneratedAnnotation(elements, "https://github.com/hansenji/ViewModelInject")
        return ViewModelInjectionModule(moduleName, public, inflationNames, generatedAnnotation)
    }

    private fun writeViewModelModule(
        elements: ViewModelModuleElements,
        module: ViewModelInjectionModule
    ) {
        val generatedTypeSpec = module.brewJava()
            .toBuilder()
            .addOriginatingElement(elements.moduleType)
            .applyEach(elements.viewModelTypes) {
                addOriginatingElement(it)
            }
            .build()
        JavaFile.builder(module.generatedType.packageName(), generatedTypeSpec)
            .addFileComment("Generated by @ViewModelModule. Do not modify!")
            .build()
            .writeTo(filer)
    }

    private fun warn(message: String, element: Element? = null) {
        messager.printMessage(WARNING, message, element)
    }

    private fun error(message: String, element: Element? = null) {
        messager.printMessage(ERROR, message, element)
    }

    private data class ViewModelInjectElements(
        val targetType: TypeElement,
        val targetConstructor: ExecutableElement
    )

    private data class ViewModelModuleElements(
        val moduleType: TypeElement,
        val viewModelTypes: List<TypeElement>
    )

}

private val BASIC_FACTORY = ClassName.get("com.vikingsen.inject.viewmodel", "ViewModelBasicFactory")
private val SAVED_STATE_FACTORY = ClassName.get("com.vikingsen.inject.viewmodel.savedstate", "ViewModelSavedStateFactory")
private val SAVED_STATE_FACTORY_KEY = listOf(
    NamedKey(Key(ClassName.get("androidx.lifecycle", "SavedStateHandle")), "savedStateHandle")
)