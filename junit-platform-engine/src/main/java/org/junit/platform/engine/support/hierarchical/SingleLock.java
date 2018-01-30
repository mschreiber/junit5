/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import java.util.Optional;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.locks.Lock;

import org.junit.platform.commons.annotation.ExecutionMode;

public class SingleLock implements AcquiredResourceLock {
	private final Lock lock;

	public SingleLock(Lock lock) {
		this.lock = lock;
	}

	@Override
	public Optional<ExecutionMode> getForcedExecutionMode() {
		return Optional.of(ExecutionMode.SameThread);
	}

	@Override
	public AcquiredResourceLock acquire() throws InterruptedException {
		ForkJoinPool.managedBlock(new SingleLockManagedBlocker());
		return this;
	}

	@Override
	public void release() {
		lock.unlock();
	}

	private class SingleLockManagedBlocker implements ForkJoinPool.ManagedBlocker {
		private boolean acquired;

		@Override
		public boolean block() throws InterruptedException {
			lock.lockInterruptibly();
			acquired = true;
			return true;
		}

		@Override
		public boolean isReleasable() {
			return acquired || lock.tryLock();
		}
	}
}