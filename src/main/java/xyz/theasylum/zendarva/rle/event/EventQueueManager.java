package xyz.theasylum.zendarva.rle.event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.theasylum.zendarva.rle.event.event.Event;
import xyz.theasylum.zendarva.rle.event.event.GuiEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

public class EventQueueManager {
    private static final Logger LOG = LogManager.getLogger(EventQueueManager.class);

    public static EventQueue.GuiEventQueue guiEventQueue = new EventQueue.GuiEventQueue();

    private static EventQueueManager myInstance;
    private Map<EventQueue, List<Object>> eventListeners;
    private Map<EventQueue, Queue<Event>> eventQueue;

    private EventQueueManager() {
        eventListeners = new HashMap<>();
        eventQueue = new HashMap<>();
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
            LOG.error("Attepted to raise event on nonexistent queue: {}", queue.getClass().getSimpleName());
            return;
        }
        eventQueue.get(queue).add(event);
    }

    public void processEvents() {
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
            LOG.error("Attepted to subscribe to a nonexistent queue: {}", queue.getClass().getSimpleName());
            return;
        }
        eventListeners.get(queue).add(obj);
    }

}
