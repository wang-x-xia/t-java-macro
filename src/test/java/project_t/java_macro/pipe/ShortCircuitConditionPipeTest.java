package project_t.java_macro.pipe;

import org.junit.jupiter.api.Test;
import project_t.java_macro.pipe.engine.MethodMappingTest;

@SuppressWarnings({"ConstantValue", "unused"})
class ShortCircuitConditionPipeTest extends MethodMappingTest {

    @Test
    void testIfTrue() {
        assertMethodEquals("ifTrue", "ifTrueResult");
    }

    public int ifTrue() {
        if (true) {
            return 1;
        } else {
            return 2;
        }
    }

    public int ifTrueResult() {
        {
            return 1;
        }
    }

    @Test
    void testIfCascade() {
        assertMethodEquals("ifCascade", "ifCascadeResult");
    }

    public int ifCascade() {
        if (true) {
            if (false) {
                return 2;
            } else {
                return 1;
            }
        } else {
            return 2;
        }
    }

    public int ifCascadeResult() {
        {
            {
                return 1;
            }
        }
    }
}