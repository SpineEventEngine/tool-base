package io.spine.tools.psi.java

import com.intellij.pom.java.LanguageLevel
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElement
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

@DisplayName("`PsiElementFactoryDecorator` should")
internal class PsiElementFactoryDecoratorSpec {

    companion object {
        private val project = Environment.project
        private val factory = JavaPsiFacade.getElementFactory(project)
        private val decorated = PsiElementFactoryDecorator(factory)
    }

    @Nested
    inner class `trim the text passed to` {

        @Test
        fun createDocTagFromText() {
            val docTag = """
                @param value The value to process.
            """
            assertDoesNotThrow {
                decorated.createDocTagFromText(docTag)
            }
        }

        @Test
        fun createDocCommentFromText() {
            val docComment = """
                /**
                 * This is a sample doc comment.
                 * @param value the value to be processed
                 * @return a computed result
                 */
            """
            assertDoesNotThrow {
                decorated.createDocCommentFromText(docComment)
                decorated.createDocCommentFromText(docComment, context)
            }
        }

        @Test
        fun createClassFromText() {
            val clazz = """
                public class MyClass {
                }
            """
            assertDoesNotThrow {
                decorated.createClassFromText(clazz, context)
            }
        }

        @Test
        fun createFieldFromText() {
            val field = """
                private int myField = 10;
            """
            assertDoesNotThrow {
                decorated.createFieldFromText(field, context)
            }
        }

        @Test
        fun createMethodFromText() {
            val method = """
                public void greet(String name) {
                   System.out.println("Hello!");
                }
            """
            assertDoesNotThrow {
                decorated.createMethodFromText(method, context)
                decorated.createMethodFromText(method, context, languageLevel)
            }
        }

        @Test
        fun createParameterFromText() {
            val parameter = """
                int number
            """
            assertDoesNotThrow {
                decorated.createParameterFromText(parameter, context)
            }
        }

        @Test
        fun createRecordHeaderFromText() {
            val recordHeader = """
                int x, String y
            """
            assertDoesNotThrow {
                decorated.createRecordHeaderFromText(recordHeader, context)
            }
        }

        @Test
        fun createResourceFromText() {
            val resource = """
                BufferedReader reader = new BufferedReader(new FileReader("file.txt"))
            """
            assertDoesNotThrow {
                decorated.createResourceFromText(resource, context)
            }
        }

        @Test
        fun createTypeFromText() {
            val type = """
                java.util.Map<String, Integer>
            """
            assertDoesNotThrow {
                decorated.createTypeFromText(type, context)
            }
        }

        @Test
        fun createTypeElementFromText() {
            val typeElement = """
                List<String>
            """
            assertDoesNotThrow {
                decorated.createTypeElementFromText(typeElement, context)
            }
        }

        @Test
        fun createReferenceFromText() {
            val reference = """
                com.example.company.MyClass
            """
            assertDoesNotThrow {
                decorated.createReferenceFromText(reference, context)
            }
        }

        @Test
        fun createCodeBlockFromText() {
            val codeBlock = """
                {
                    int x = 10;
                    System.out.println(x);
                }
            """
            assertDoesNotThrow {
                decorated.createCodeBlockFromText(codeBlock, context)
            }
        }

        @Test
        fun createStatementFromText() {
            val statement = """
                int x = 10;
            """
            assertDoesNotThrow {
                decorated.createStatementFromText(statement, context)
            }
        }

        @Test
        fun createExpressionFromText() {
            val expression = """
                a + b
            """
            assertDoesNotThrow {
                decorated.createExpressionFromText(expression, context)
            }
        }

        @Test
        fun createCommentFromText() {
            val comment = """
                // This is a comment.
            """
            assertDoesNotThrow {
                decorated.createCommentFromText(comment, context)
            }
        }

        @Test
        fun createTypeParameterFromText() {
            val typeParameter = """
                T extends Comparable<T>
            """
            assertDoesNotThrow {
                decorated.createTypeParameterFromText(typeParameter, context)
            }
        }

        @Test
        fun createAnnotationFromText() {
            val annotation = """
                @Override
            """
            assertDoesNotThrow {
                decorated.createAnnotationFromText(annotation, context)
            }
        }

        @Test
        fun createEnumConstantFromText() {
            val constant = """
                VALUE
            """
            assertDoesNotThrow {
                decorated.createEnumConstantFromText(constant, context)
            }
        }

        @Test
        fun createPrimitiveTypeFromText() {
            val primitive = """
                int
            """
            assertDoesNotThrow {
                decorated.createPrimitiveTypeFromText(primitive)
            }
        }

        @Test
        fun createModuleFromText() {
            val module = """
                module com.example.module {
                    requires java.base;
                    exports com.example;
                }
            """
            assertDoesNotThrow {
                decorated.createModuleFromText(module, context)
            }
        }

        @Test
        fun createModuleStatementFromText() {
            val statement = """
                requires java.logging;
            """
            assertDoesNotThrow {
                decorated.createModuleStatementFromText(statement, context)
            }
        }

        @Test
        fun createModuleReferenceFromText() {
            val reference = """
                java.desktop
            """
            assertDoesNotThrow {
                decorated.createModuleReferenceFromText(reference, context)
            }
        }
    }
}

private val context: PsiElement? = null
private val languageLevel = LanguageLevel.JDK_11
