package edu.cit.alvarado.channel;

final class TianggeOrderContext {
    private static final ThreadLocal<Boolean> ACTIVE = ThreadLocal.withInitial(() -> false);

    static void begin() { ACTIVE.set(true); }
    static void end() { ACTIVE.remove(); }
    static boolean active() { return ACTIVE.get(); }
}
