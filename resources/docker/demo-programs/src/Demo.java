import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

public class Demo {

	public static void main(String[] args) {

		int program = Integer.parseInt( args[0] );
		if ( program == 1 ) {
			Demo1.doYourThing();
		}
		else if ( program == 2 ) {
			Demo2.doYourThing();
		}
		else if ( program == 3 ) {
			Demo3 demo = new Demo3();
			demo.doYourThing();
		}
		else if ( program == 4 ) {
			Demo4 demo = new Demo4();
			demo.doYourThing();
		}
		else {
			System.err.println( "Unknown program code" );
		}
	}

	/**
	 * Simple (brute force) recursive Fibonacci algorithm. Full utilization of CPU.
	 */
	public static class Demo1 {
		// Default Fibonacci number to calculate
		private static final int N = 100;

		public static void doYourThing() {
			for ( int i = 1; i <= N; i++ ) {
				System.out.println( fib( i ) );
			}
		}

		public static long fib(int n) {
			if ( n <= 1 ) {
				return n;
			}
			else {
				return fib( n - 1 ) + fib( n - 2 );
			}
		}
	}

	/**
	 * File system scanning. A lot of kernel/system time.
	 */
	public static class Demo2 {
		// Default file root for find demo
		private static final String ROOT = "/";
		private static final int MAX_THREAD_COUNT = 10;

		public static void doYourThing() {
			for ( int i = 0; i < MAX_THREAD_COUNT; i++ ) {
				Thread thread = new Thread( new DiskScanner( new File( ROOT ) ) );
				thread.setName( "Demo 2 - Scanner Thread #" + i );
				thread.start();
			}
		}

		static class DiskScanner implements Runnable {
			private final File file;

			DiskScanner(File file) {
				this.file = file;
			}

			public void run() {
				scanFileTree( file );
			}

			public void scanFileTree(File file) {
				System.out.println( Thread.currentThread().getName() + ": " + file.length() );
				if ( file.isFile() ) {
					Stream<String> lines = null;
					try {
						lines = Files.lines( Paths.get( file.getAbsolutePath() ) );
						Optional<String> hasPassword = lines.filter( s -> s.contains( "foo" ) ).findFirst();
						if ( hasPassword.isPresent() ) {
							System.out.println( hasPassword.get() );
						}
					}
					catch (Exception e) {
						// ignore
					}
					finally {
						if ( lines != null ) {
							lines.close();
						}
					}
				}
				else if ( file.isDirectory() ) {
					File[] dirContent = file.listFiles();
					if ( dirContent != null ) {
						for ( File f : dirContent ) {
							scanFileTree( f );
						}
					}
				}
			}
		}
	}

	/**
	 * Concurrent program with deadlock
	 */
	public static class Demo3 {
		private final CountDownLatch startSignal = new CountDownLatch( 1 );
		private final ReentrantLock lockA = new ReentrantLock();
		private final ReentrantLock lockB = new ReentrantLock();

		private final Runnable runner1;
		private final Runnable runner2;

		enum LockOrder {
			A_THEN_B,
			B_THEN_A
		}

		public Demo3() {
			this.runner1 = new Interlocker( LockOrder.A_THEN_B );
			this.runner2 = new Interlocker( LockOrder.B_THEN_A );
		}

		public void doYourThing() {
			Thread thread1 = new Thread( runner1 );
			thread1.setName( "Demo 3 Thread 1" );
			thread1.start();

			Thread thread2 = new Thread( runner2 );
			thread2.setName( "Demo 3 Thread 2" );
			thread2.start();

			startSignal.countDown();
		}

		class Interlocker implements Runnable {
			private final Lock firstLock;
			private final Lock secondLock;

			Interlocker(LockOrder lockOrder) {
				if ( LockOrder.A_THEN_B.equals( lockOrder ) ) {
					this.firstLock = lockA;
					this.secondLock = lockB;
				}
				else {
					this.firstLock = lockB;
					this.secondLock = lockA;
				}
			}

			public void run() {
				try {
					startSignal.await();
					doWork();
				}
				catch (InterruptedException ex) {
					// ignore
				}
			}

			public void doWork() {
				firstLock.lock();
				try {
					System.out.println( "1" );
					secondLock.lock();
					try {
						System.out.println( "2" );
						// do something
					}
					finally {
						secondLock.unlock();
					}
				}
				finally {
					firstLock.unlock();
				}
			}
		}
	}

	/**
	 * Heap allocation example.
	 */
	public static class Demo4 {
		private static final Random random = new Random();
		private static final double SURVIVOR_RATIO = 0.5;
		private List<Dummy> dummyList = new ArrayList<>();

		public void doYourThing() {
			try {
				while ( true ) {
					double randomDouble = random.nextDouble();
					Dummy dummy = new Dummy();
					dummy.setValue( random.nextInt( Dummy.ARRAY_ALLOCATION_SIZE ), 1 );
					if ( randomDouble < SURVIVOR_RATIO ) {
						dummyList.add( dummy );
						if ( dummyList.size() % 100 == 0 ) {
							System.out.println( dummyList.size() );
						}
					}

					Thread.sleep( 5 );
				}
			}
			catch (OutOfMemoryError e) {
				System.out.println( "Reached memory max" );
			}
			catch (InterruptedException e) {
				System.out.println( "Interrupted Exception" );
			}
		}
	}

	public static class Dummy {
		public static final int ARRAY_ALLOCATION_SIZE = 1000;
		private long[] longArray = new long[ARRAY_ALLOCATION_SIZE];

		public void setValue(int index, long value) {
			longArray[index] = value;
		}
	}
}