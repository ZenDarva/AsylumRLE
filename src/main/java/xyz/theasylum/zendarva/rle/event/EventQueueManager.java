package xyz.theasylum.zendarva.rle.event;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.theasylum.zendarva.rle.event.event.Event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class EventQueueManager {
    private static final Logger LOG = LogManager.getLogger(EventQueueManager.class);

    public static EventQueue.GuiEventQueue guiEventQueue = new EventQueue.GuiEventQueue();

    private static EventQueueManager myInstance;
    private Map<EventQueue, List<Object>> eventListeners;
    private Map<EventQueue, Queue<Event>> eventQueue;
    private List<Pair<EventQueue, Event>> eventWaitingLine;
    private List<Pair<EventQueue, Object>> subscribeWaitingLine;
    private List<Pair<EventQueue, Object>> unsubscribeWaitingLine;

    private EventQueueManager() {
        eventListeners = new HashMap<>();
        eventQueue = new HashMap<>();
        eventWaitingLine = new LinkedList();
        subscribeWaitingLine= new LinkedList<>();
        unsubscribeWaitingLine= new LinkedList<>();

        addQueue(guiEventQueue);
    }

    public static EventQueueManager instance() {
        if (myInstance == null) {
            myInstance = new EventQueueManager();
        }
        return myInstance;
    }

    public void addQueue(EventQueue queue) {
        eventListeners.put(queue, new LinkedList<>());
        eventQueue.put(queue, new LinkedList<>());
    }

    public void raiseEvent(EventQueue queue, Event event) {
        if (!eventListeners.containsKey(queue)) {
            LOG.error("Attempted to raise event on nonexistent queue: {}", queue.getClass().getSimpleName());
            return;
        }
        eventWaitingLine.add(new ImmutablePair<EventQueue, Event>(queue,event));
    }

    public void processEvents() {
        eventWaitingLine.stream().forEach(f->eventQueue.get(f.getLeft()).add(f.getRight()));
        subscribeWaitingLine.stream().forEach(f->eventListeners.get(f.getLeft()).add(f.getRight()));
        unsubscribeWaitingLine.stream().forEach(f->eventListeners.get(f.getLeft()).remove(f.getRight()));
        eventWaitingLine.clear();
        subscribeWaitingLine.clear();
        unsubscribeWaitingLine.clear();
        for (EventQueue queue : eventQueue.keySet()) {
            while (!eventQueue.get(queue).isEmpty()) {
                Event event = eventQueue.get(queue).poll();
                for (Object o : eventListeners.get(queue)) {
                    pushEvent(o, event);
                }
            }
        }
    }

    private void pushEvent(Object obj, Event event) {
        for (Method declaredMethod : obj.getClass().getDeclaredMethods()) {
            if (declaredMethod.getParameterCount() == 1 && event.getClass().isAssignableFrom(declaredMethod.getParameters()[0].getType())) {
                declaredMethod.setAccessible(true);
                try {
                    declaredMethod.invoke(obj, event);
                } catch (IllegalAccessException e) {
                } catch (InvocationTargetException e) {
                }
            }
        }
    }

    public void subscribeQueue(EventQueue queue, Object obj) {
        if (!eventListeners.containsKey(queue)) {
            LOG.error("Attempted to subscribe to a nonexistent queue: {}", queue.getClass().getSimpleName());
            return;
        }
        //eventListeners.get(queue).add(obj);
        subscribeWaitingLine.add(new ImmutablePair<>(queue,obj));
    }

    public void unsubscribeQueue(EventQueue queue, Object obj) {
        if (!eventListeners.containsKey(queue)) {
            LOG.error("Attempted to unsubscribe from a nonexistent queue: {}", queue.getClass().getSimpleName());
            return;
        }
        //eventListeners.get(queue).remove(obj);
        unsubscribeWaitingLine.add(new ImmutablePair<>(queue,obj));
    }

}
