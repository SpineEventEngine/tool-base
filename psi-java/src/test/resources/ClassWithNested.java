/*
 * Copyright 2023, TeamDev. All rights reserved.
 */
package given.java.source;

/**
 * This source code is used Java source code parsing tests.
 */
public class ClassWithNested {

    public static class InnerOne {

        private class NonStaticInner {
            private int value;
        }
    }

    private class InterimInner {
        private String value;
    }

    public static class InnerTwo {

        public static enum MyEnum {
            ONE, TWO, THREE
        }

        @Subscribe
        public static @Nullable MyEnum pickOne(int seed) {
            return MyEnum.ONE;
        }
    }
}
