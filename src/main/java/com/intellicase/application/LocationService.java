package com.intellicase.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Service to manage global FBI locations (Domestic Field Offices and International Legats).
 */
public class LocationService {
    private static final Map<String, List<String>> GLOBAL_LOCATIONS = new TreeMap<>();

    static {
        // North America (Domestic)
        GLOBAL_LOCATIONS.put("USA (Field Offices)", List.of(
            "Albany, NY", "Albuquerque, NM", "Anchorage, AK", "Atlanta, GA", "Baltimore, MD", 
            "Birmingham, AL", "Boston, MA", "Buffalo, NY", "Charlotte, NC", "Chicago, IL", 
            "Cincinnati, OH", "Cleveland, OH", "Columbia, SC", "Dallas, TX", "Denver, CO", 
            "Detroit, MI", "El Paso, TX", "Honolulu, HI", "Houston, TX", "Indianapolis, IN", 
            "Jackson, MS", "Jacksonville, FL", "Kansas City, MO", "Knoxville, TN", "Las Vegas, NV", 
            "Little Rock, AR", "Los Angeles, CA", "Louisville, KY", "Memphis, TN", "Miami, FL", 
            "Milwaukee, WI", "Minneapolis, MN", "Mobile, AL", "New Haven, CT", "New Orleans, LA", 
            "New York City, NY", "Newark, NJ", "Norfolk, VA", "Oklahoma City, OK", "Omaha, NE", 
            "Philadelphia, PA", "Phoenix, AZ", "Pittsburgh, PA", "Portland, OR", "Richmond, VA", 
            "Sacramento, CA", "Salt Lake City, UT", "San Antonio, TX", "San Diego, CA", 
            "San Francisco, CA", "San Juan, PR", "Seattle, WA", "Springfield, IL", "St. Louis, MO", 
            "Tampa, FL", "Washington, D.C."
        ));

        // Europe (Legats)
        GLOBAL_LOCATIONS.put("Europe (Legats)", List.of(
            "London, UK", "Paris, France", "Berlin, Germany", "Rome, Italy", "Madrid, Spain",
            "Brussels, Belgium", "Amsterdam, Netherlands", "Vienna, Austria", "Copenhagen, Denmark",
            "Stockholm, Sweden", "Warsaw, Poland", "Prague, Czech Republic", "Athens, Greece"
        ));

        // Asia & Pacific (Legats)
        GLOBAL_LOCATIONS.put("Asia/Pacific (Legats)", List.of(
            "Tokyo, Japan", "Seoul, South Korea", "Beijing, China", "Bangkok, Thailand",
            "Singapore, Singapore", "New Delhi, India", "Canberra, Australia", "Wellington, New Zealand",
            "Manila, Philippines", "Jakarta, Indonesia"
        ));

        // Middle East & Africa (Legats)
        GLOBAL_LOCATIONS.put("Middle East/Africa (Legats)", List.of(
            "Riyadh, Saudi Arabia", "Abu Dhabi, UAE", "Tel Aviv, Israel", "Cairo, Egypt",
            "Nairobi, Kenya", "Pretoria, South Africa", "Lagos, Nigeria", "Rabat, Morocco"
        ));

        // Americas (International Legats)
        GLOBAL_LOCATIONS.put("Americas (Legats)", List.of(
            "Ottawa, Canada", "Mexico City, Mexico", "Brasilia, Brazil", "Buenos Aires, Argentina",
            "Bogota, Colombia", "Santiago, Chile", "Panama City, Panama"
        ));
    }

    public List<String> getRegions() {
        List<String> regions = new ArrayList<>(GLOBAL_LOCATIONS.keySet());
        Collections.sort(regions);
        return regions;
    }

    public List<String> getLocationsByRegion(String region) {
        return GLOBAL_LOCATIONS.getOrDefault(region, Collections.emptyList());
    }

    public List<String> getAllLocations() {
        List<String> all = new ArrayList<>();
        for (List<String> list : GLOBAL_LOCATIONS.values()) {
            all.addAll(list);
        }
        Collections.sort(all);
        return all;
    }
}
