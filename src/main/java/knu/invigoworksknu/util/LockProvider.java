package knu.invigoworksknu.util;

public interface LockProvider {

    boolean tryLock(String key, long timeoutMs);

    void releaseLock(String key);
}
