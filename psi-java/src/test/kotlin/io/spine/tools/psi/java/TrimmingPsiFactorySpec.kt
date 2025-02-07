package io.spine.tools.psi.java

import com.intellij.pom.java.LanguageLevel
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElement
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

@DisplayName("`TrimmingPsiFactory` should trim the text passed to")
internal class TrimmingPsiFactorySpec {

    companion object {
        private val project = Environment.project
        private val factory = JavaPsiFacade.getElementFactory(project)
        private val trimmingFactory = TrimmingPsiFactory(factory)
    }

    @Test
    fun createFieldFromText() {
        val field = """
            private int myField = 10;
        """
        assertDoesNotThrow {
            trimmingFactory.createFieldFromText(field, context)
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
            trimmingFactory.createMethodFromText(method, context)
            trimmingFactory.createMethodFromText(method, context, languageLevel)
        }
    }

    @Test
    fun createParameterFromText() {
        val parameter = """
            int number
        """
        assertDoesNotThrow {
            trimmingFactory.createParameterFromText(parameter, context)
        }
    }

    @Test
    fun createRecordHeaderFromText() {
        val recordHeader = """
            int x, String y
        """
        assertDoesNotThrow {
            trimmingFactory.createRecordHeaderFromText(recordHeader, context)
        }
    }

    @Test
    fun createResourceFromText() {
        val resource = """
            BufferedReader reader = new BufferedReader(new FileReader("file.txt"))
        """
        assertDoesNotThrow {
            trimmingFactory.createResourceFromText(resource, context)
        }
    }

    @Test
    fun createTypeFromText() {
        val type = """
            java.util.Map<String, Integer>
        """
        assertDoesNotThrow {
            trimmingFactory.createTypeFromText(type, context)
        }
    }

    @Test
    fun createTypeElementFromText() {
        val typeElement = """
            List<String>
        """
        assertDoesNotThrow {
            trimmingFactory.createTypeElementFromText(typeElement, context)
        }
    }

    @Test
    fun createReferenceFromText() {
        val reference = """
            com.example.company.MyClass
        """
        assertDoesNotThrow {
            trimmingFactory.createReferenceFromText(reference, context)
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
            trimmingFactory.createCodeBlockFromText(codeBlock, context)
        }
    }

    @Test
    fun createStatementFromText() {
        val statement = """
            int x = 10;
        """
        assertDoesNotThrow {
            trimmingFactory.createStatementFromText(statement, context)
        }
    }

    @Test
    fun createExpressionFromText() {
        val expression = """
            a + b
        """
        assertDoesNotThrow {
            trimmingFactory.createExpressionFromText(expression, context)
        }
    }

    @Test
    fun createCommentFromText() {
        val comment = """
            // This is a comment.
        """
        assertDoesNotThrow {
            trimmingFactory.createCommentFromText(comment, context)
        }
    }

    @Test
    fun createTypeParameterFromText() {
        val typeParameter = """
            T extends Comparable<T>
        """
        assertDoesNotThrow {
            trimmingFactory.createTypeParameterFromText(typeParameter, context)
        }
    }

    @Test
    fun createAnnotationFromText() {
        val annotation = """
            @Override
        """
        assertDoesNotThrow {
            trimmingFactory.createAnnotationFromText(annotation, context)
        }
    }

    @Test
    fun createEnumConstantFromText() {
        val enumConstant = """
            VALUE
        """
        assertDoesNotThrow {
            trimmingFactory.createEnumConstantFromText(enumConstant, context)
        }
    }

    @Test
    fun createPrimitiveTypeFromText() {
        val primitiveType = """
            int
        """
        assertDoesNotThrow {
            trimmingFactory.createPrimitiveTypeFromText(primitiveType)
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
            trimmingFactory.createModuleFromText(module, context)
        }
    }
}

private val context: PsiElement? = null
private val languageLevel = LanguageLevel.JDK_11
