public class A {
    public static class Test0 {
        public static void staticCall(){}
    }

    public interface Test1 {
        static void staticCall(){}
    }

    public record Test2() {
        public static void staticCall(){}
    }

    public enum Test3 {
        ;
        static void staticCall(){}
    }

    public void foo() {
        Test0.staticCall();
        Test1.staticCall();
        Test2.staticCall();
        Test3.staticCall();
    }
}
