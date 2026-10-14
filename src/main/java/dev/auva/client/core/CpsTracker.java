package dev.auva.client.core;

import java.util.ArrayDeque;
import java.util.Deque;

/** Records GLFW mouse events; avoids a 20Hz tick sampling ceiling. */
public final class CpsTracker {
    private static final Deque<Long> LEFT=new ArrayDeque<>(),RIGHT=new ArrayDeque<>();
    private CpsTracker(){}
    public static synchronized void click(int button){
        if(button==0)LEFT.addLast(System.nanoTime());
        if(button==1)RIGHT.addLast(System.nanoTime());
        prune();
    }
    public static synchronized void prune(){
        long deadline=System.nanoTime()-1_000_000_000L;
        while(!LEFT.isEmpty()&&LEFT.peekFirst()<deadline)LEFT.removeFirst();
        while(!RIGHT.isEmpty()&&RIGHT.peekFirst()<deadline)RIGHT.removeFirst();
    }
    public static synchronized int left(){prune();return LEFT.size();}
    public static synchronized int right(){prune();return RIGHT.size();}
}
