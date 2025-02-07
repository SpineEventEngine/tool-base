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
        private val decorator = PsiElementFactoryDecorator(factory)
    }

    @Nested
    inner class `trim the text passed to` {

        @Test
        fun createDocTagFromText() {
            val docTag = """
                @param value The value to process.
            """
            assertDoesNotThrow {
                decorator.createDocTagFromText(docTag)
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
                decorator.createDocCommentFromText(docComment)
                decorator.createDocCommentFromText(docComment, context)
            }
        }

        @Test
        fun createClassFromText() {
            val clazz = """
                public class MyClass {
                }
            """
            assertDoesNotThrow {
                decorator.createClassFromText(clazz, context)
            }
        }

        @Test
        fun createFieldFromText() {
            val field = """
                private int myField = 10;
            """
            assertDoesNotThrow {
                decorator.createFieldFromText(field, context)
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
                decorator.createMethodFromText(method, context)
                decorator.createMethodFromText(method, context, languageLevel)
            }
        }

        @Test
        fun createParameterFromText() {
            val parameter = """
                int number
            """
            assertDoesNotThrow {
                decorator.createParameterFromText(parameter, context)
            }
        }

        @Test
        fun createRecordHeaderFromText() {
            val recordHeader = """
                int x, String y
            """
            assertDoesNotThrow {
                decorator.createRecordHeaderFromText(recordHeader, context)
            }
        }

        @Test
        fun createResourceFromText() {
            val resource = """
                BufferedReader reader = new BufferedReader(new FileReader("file.txt"))
            """
            assertDoesNotThrow {
                decorator.createResourceFromText(resource, context)
            }
        }

        @Test
        fun createTypeFromText() {
            val type = """
                java.util.Map<String, Integer>
            """
            assertDoesNotThrow {
                decorator.createTypeFromText(type, context)
            }
        }

        @Test
        fun createTypeElementFromText() {
            val typeElement = """
                List<String>
            """
            assertDoesNotThrow {
                decorator.createTypeElementFromText(typeElement, context)
            }
        }

        @Test
        fun createReferenceFromText() {
            val reference = """
                com.example.company.MyClass
            """
            assertDoesNotThrow {
                decorator.createReferenceFromText(reference, context)
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
                decorator.createCodeBlockFromText(codeBlock, context)
            }
        }

        @Test
        fun createStatementFromText() {
            val statement = """
                int x = 10;
            """
            assertDoesNotThrow {
                decorator.createStatementFromText(statement, context)
            }
        }

        @Test
        fun createExpressionFromText() {
            val expression = """
                a + b
            """
            assertDoesNotThrow {
                decorator.createExpressionFromText(expression, context)
            }
        }

        @Test
        fun createCommentFromText() {
            val comment = """
                // This is a comment.
            """
            assertDoesNotThrow {
                decorator.createCommentFromText(comment, context)
            }
        }

        @Test
        fun createTypeParameterFromText() {
            val typeParameter = """
                T extends Comparable<T>
            """
            assertDoesNotThrow {
                decorator.createTypeParameterFromText(typeParameter, context)
            }
        }

        @Test
        fun createAnnotationFromText() {
            val annotation = """
                @Override
            """
            assertDoesNotThrow {
                decorator.createAnnotationFromText(annotation, context)
            }
        }

        @Test
        fun createEnumConstantFromText() {
            val constant = """
                VALUE
            """
            assertDoesNotThrow {
                decorator.createEnumConstantFromText(constant, context)
            }
        }

        @Test
        fun createPrimitiveTypeFromText() {
            val primitive = """
                int
            """
            assertDoesNotThrow {
                decorator.createPrimitiveTypeFromText(primitive)
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
                decorator.createModuleFromText(module, context)
            }
        }

        @Test
        fun createModuleStatementFromText() {
            val statement = """
                requires java.logging;
            """
            assertDoesNotThrow {
                decorator.createModuleStatementFromText(statement, context)
            }
        }

        @Test
        fun createModuleReferenceFromText() {
            val reference = """
                java.desktop
            """
            assertDoesNotThrow {
                decorator.createModuleReferenceFromText(reference, context)
            }
        }
    }
}

private val context: PsiElement? = null
private val languageLevel = LanguageLevel.JDK_11
