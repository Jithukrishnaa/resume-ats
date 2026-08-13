package resume_ats.util.extractor;

import resume_ats.util.cleaner.TextCleaner;

import java.util.LinkedHashSet;
import java.util.Set;

public final class CertificationExtractor {

    private CertificationExtractor() {
    }

    private static final String[] CERTIFICATIONS = {

            // AWS
            "aws",
            "aws certified",
            "aws cloud practitioner",
            "aws solutions architect",
            "aws developer",

            // Azure
            "azure",
            "microsoft azure",
            "azure fundamentals",
            "azure administrator",

            // Google
            "google cloud",
            "gcp",
            "associate cloud engineer",
            "professional cloud architect",

            // Oracle
            "oracle certified",
            "oracle java",
            "ocjp",
            "ocp",

            // Java
            "java se",
            "java professional",

            // Scrum
            "scrum",
            "scrum master",
            "psm",
            "csm",

            // PMI
            "pmp",
            "project management professional",

            // Cisco
            "ccna",
            "ccnp",
            "cisco certified",

            // RedHat
            "rhcsa",
            "rhce",

            // Salesforce
            "salesforce",
            "salesforce administrator",

            // AI / ML
            "tensorflow developer",
            "azure ai",
            "google ai",
            "machine learning specialization",

            // Data
            "power bi",
            "tableau",

            // Misc
            "itil",
            "comptia",
            "linux foundation"
    };

    /**
     * Extract certifications.
     */
    public static Set<String> extractCertifications(String text) {

        LinkedHashSet<String> certifications = new LinkedHashSet<>();

        if (text == null || text.isBlank()) {
            return certifications;
        }

        text = TextCleaner.normalizeForMatching(text);

        for (String cert : CERTIFICATIONS) {

            if (text.contains(cert.toLowerCase())) {

                certifications.add(normalize(cert));

            }
        }

        return certifications;
    }

    /**
     * Comma separated list.
     */
    public static String extractCertificationString(String text) {

        return String.join(", ",
                extractCertifications(text));

    }

    /**
     * Count certifications.
     */
    public static int count(String text) {

        return extractCertifications(text).size();

    }

    /**
     * Compare JD vs Resume certifications.
     */
    public static double calculateMatch(
            Set<String> resumeCerts,
            Set<String> jdCerts) {

        if (jdCerts.isEmpty())
            return 1.0;

        int matched = 0;

        for (String cert : jdCerts) {

            if (resumeCerts.contains(cert)) {
                matched++;
            }
        }

        return (double) matched / jdCerts.size();
    }

    /**
     * Normalize names.
     */
    private static String normalize(String cert) {

        cert = cert.toLowerCase();

        if (cert.contains("aws"))
            return "AWS";

        if (cert.contains("azure"))
            return "Microsoft Azure";

        if (cert.contains("google"))
            return "Google Cloud";

        if (cert.contains("oracle"))
            return "Oracle Java";

        if (cert.contains("scrum"))
            return "Scrum Master";

        if (cert.contains("pmp"))
            return "PMP";

        if (cert.contains("ccna"))
            return "CCNA";

        if (cert.contains("ccnp"))
            return "CCNP";

        if (cert.contains("rhcsa"))
            return "RHCSA";

        if (cert.contains("rhce"))
            return "RHCE";

        if (cert.contains("salesforce"))
            return "Salesforce";

        if (cert.contains("power bi"))
            return "Power BI";

        if (cert.contains("tableau"))
            return "Tableau";

        return cert.toUpperCase();
    }

}