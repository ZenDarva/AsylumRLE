package xyz.theasylum.zendarva.rle.utility;

import java.util.HashMap;

public class Profile {
    static HashMap<Object, Long> profileData = new HashMap<>();

    public static void start(Object obj){
        profileData.put(obj, System.nanoTime());
    }
    public static void stop(Object obj){

        long start = profileData.remove(obj);
        double seconds =((double)System.nanoTime()-(double)start) / (double)1_000_000_000;

        System.out.println("Profile: " + (System.nanoTime()-start) + " nano, " + seconds + "seconds");
    }
}
