package resume_ats.util.extractor;

import resume_ats.util.cleaner.TextCleaner;

import java.util.LinkedHashSet;
import java.util.Set;

public final class LocationExtractor {

    private LocationExtractor() {
    }

    /*
     * Frequently used Indian cities.
     * Can easily be expanded later.
     */
    private static final String[] CITIES = {

            "bangalore",
            "bengaluru",
            "hyderabad",
            "chennai",
            "mumbai",
            "pune",
            "delhi",
            "noida",
            "gurgaon",
            "gurugram",
            "kochi",
            "ernakulam",
            "trivandrum",
            "thiruvananthapuram",
            "palakkad",
            "coimbatore",
            "madurai",
            "mysore",
            "kolkata",
            "ahmedabad",
            "jaipur",
            "lucknow",
            "vizag",
            "vijayawada",
            "nagpur",
            "bhopal"
    };

    /*
     * States
     */
    private static final String[] STATES = {

            "kerala",
            "tamil nadu",
            "karnataka",
            "maharashtra",
            "telangana",
            "andhra pradesh",
            "gujarat",
            "rajasthan",
            "uttar pradesh",
            "west bengal"
    };

    /**
     * Extract first detected location.
     */
    public static String extractLocation(String text) {

        if (text == null || text.isBlank()) {
            return "";
        }

        text = TextCleaner.normalizeForMatching(text);

        for (String city : CITIES) {

            if (text.contains(city)) {
                return capitalize(city);
            }
        }

        for (String state : STATES) {

            if (text.contains(state)) {
                return capitalize(state);
            }
        }

        return "";
    }

    /**
     * Detect Remote jobs.
     */
    public static boolean isRemote(String text) {

        if (text == null)
            return false;

        text = TextCleaner.normalizeForMatching(text);

        return text.contains("remote")
                || text.contains("work from home")
                || text.contains("wfh");
    }

    /**
     * Detect Hybrid jobs.
     */
    public static boolean isHybrid(String text) {

        if (text == null)
            return false;

        text = TextCleaner.normalizeForMatching(text);

        return text.contains("hybrid");
    }

    /**
     * Detect On-site jobs.
     */
    public static boolean isOnsite(String text) {

        if (text == null)
            return false;

        text = TextCleaner.normalizeForMatching(text);

        return text.contains("onsite")
                || text.contains("on site");
    }

    /**
     * Detect relocation.
     */
    public static boolean willingToRelocate(String text) {

        if (text == null)
            return false;

        text = TextCleaner.normalizeForMatching(text);

        return text.contains("relocate")
                || text.contains("willing to relocate")
                || text.contains("open to relocate");
    }

    /**
     * Compare JD location vs Resume location.
     */
    public static double calculateLocationScore(
            String resumeLocation,
            String jdLocation,
            boolean remoteJob) {

        if (remoteJob)
            return 1.0;

        if (jdLocation == null || jdLocation.isBlank())
            return 1.0;

        if (resumeLocation == null || resumeLocation.isBlank())
            return 0;

        if (resumeLocation.equalsIgnoreCase(jdLocation))
            return 1.0;

        return 0;
    }

    /**
     * Extract every mentioned location.
     */
    public static Set<String> extractAllLocations(String text) {

        LinkedHashSet<String> locations = new LinkedHashSet<>();

        if (text == null)
            return locations;

        text = TextCleaner.normalizeForMatching(text);

        for (String city : CITIES) {

            if (text.contains(city)) {
                locations.add(capitalize(city));
            }
        }

        for (String state : STATES) {

            if (text.contains(state)) {
                locations.add(capitalize(state));
            }
        }

        return locations;
    }

    /**
     * Title Case
     */
    private static String capitalize(String value) {

        String[] words = value.split(" ");

        StringBuilder builder = new StringBuilder();

        for (String word : words) {

            if (!builder.isEmpty()) {
                builder.append(" ");
            }

            builder.append(
                    Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1));
        }

        return builder.toString();
    }

}