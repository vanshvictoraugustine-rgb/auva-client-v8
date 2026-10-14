package dev.auva.client.core;

import java.util.ArrayDeque;
import java.util.Deque;

public final class NotificationCenter {
    public record Toast(String title,String body,long created){}
    private static final Deque<Toast> QUEUE=new ArrayDeque<>();
    private NotificationCenter(){}
    public static synchronized void push(String title,String body){
        while(QUEUE.size()>=4)QUEUE.removeFirst();
        QUEUE.addLast(new Toast(title,body,System.currentTimeMillis()));
    }
    public static synchronized Toast[] active(){
        long now=System.currentTimeMillis();
        while(!QUEUE.isEmpty() && now-QUEUE.peekFirst().created()>3200)QUEUE.removeFirst();
        return QUEUE.toArray(Toast[]::new);
    }
    public static synchronized void clear(){QUEUE.clear();}
}
