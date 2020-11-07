package com.vikingsen.inject.viewmodel.processor.internal

import com.squareup.inject.assisted.processor.internal.toClassName
import com.squareup.javapoet.AnnotationSpec
import javax.annotation.processing.Processor
import javax.lang.model.SourceVersion
import javax.lang.model.SourceVersion.RELEASE_8
import javax.lang.model.util.Elements

/**
 * Create a `@Generated` annotation using the correct type based on source version and availability
 * on the compilation classpath, a `value` with the fully-qualified class name of the calling
 * [Processor], and a comment pointing to this project's GitHub repo. Returns `null` if no
 * annotation type is available on the classpath.
 */
fun Processor.createGeneratedAnnotation(
    sourceVersion: SourceVersion,
    elements: Elements,
    comments: String = "https://github.com/hansenji/ViewModelInject"
): AnnotationSpec? {
  val annotationTypeName = when {
      sourceVersion <= RELEASE_8 -> "javax.annotation.Generated"
      else -> "javax.annotation.processing.Generated"
  }
  val generatedType = elements.getTypeElement(annotationTypeName) ?: return null
  return AnnotationSpec.builder(generatedType.toClassName())
      .addMember("value", "\$S", javaClass.name)
      .addMember("comments", "\$S", comments)
      .build()
}
