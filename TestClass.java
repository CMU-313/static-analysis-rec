package test;
public class TestClass {
	public void foo() {
		int i = 1;
		i += Math.random();
		try {
			if (i > 5)
				throw new RuntimeException();
		} catch (RuntimeException e) {
			Math.sin(.3);
		}

	}

	public int bar() {
		int i = 1;
		Math.sin(.3);
		i += Math.random();
		System.out.println(i);
		try {
		} catch (RuntimeException e) {
			Math.sin(.3);
		}
		return 42;
	}

	public void foobar() {
		int i = bar();
		if (i > 1) {
			i += Math.random();
			System.out.println(i);
			return;
		}
		Math.sin(.3);
	}
}