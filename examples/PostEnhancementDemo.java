import model.Post;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Demonstration of the enhanced post feature with Class Activities and School Announcements
 */
public class PostEnhancementDemo {
    
    public static void main(String[] args) {
        System.out.println("=== Enhanced Post Feature Demo ===\n");
        
        // Demo 1: Create a Class Activity Post
        System.out.println("1. Class Activity Example:");
        Post classActivity = new Post("Math Quiz Tomorrow", 
                                    "Don't forget we have a math quiz covering chapters 1-3 tomorrow. Please review your homework!", 
                                    1, 101); // teacher ID 1, class ID 101
        classActivity.setPostType(Post.TYPE_CLASS_ACTIVITY);
        classActivity.setVisibility("PARENTS_ONLY");
        classActivity.setScheduledDate(LocalDate.now().plusDays(1));
        
        System.out.println("   Title: " + classActivity.getTitle());
        System.out.println("   Type: " + classActivity.getPostTypeDisplay());
        System.out.println("   Is Class Activity: " + classActivity.isClassActivity());
        System.out.println("   Visibility: " + classActivity.getVisibilityDisplay());
        System.out.println("   Scheduled: " + classActivity.getScheduledDate());
        System.out.println();
        
        // Demo 2: Create a School Announcement
        System.out.println("2. School Announcement Example:");
        Post schoolAnnouncement = new Post("School Holiday - National Day", 
                                         "The school will be closed on September 2nd in observance of National Day. Classes will resume on September 3rd.", 
                                         2, "HOLIDAY", LocalDate.of(2025, 9, 2)); // principal ID 2, holiday category, event date
        schoolAnnouncement.setPinned(true); // Important announcement
        schoolAnnouncement.setVisibility("ALL");
        
        System.out.println("   Title: " + schoolAnnouncement.getTitle());
        System.out.println("   Type: " + schoolAnnouncement.getPostTypeDisplay());
        System.out.println("   Is School Announcement: " + schoolAnnouncement.isSchoolAnnouncement());
        System.out.println("   Category: " + schoolAnnouncement.getCategoryDisplay());
        System.out.println("   Event Date: " + schoolAnnouncement.getEventDate());
        System.out.println("   Is Pinned: " + schoolAnnouncement.isPinned());
        System.out.println("   Is Upcoming Event: " + schoolAnnouncement.isUpcomingEvent());
        System.out.println();
        
        // Demo 3: Create an Event Announcement
        System.out.println("3. Event Announcement Example:");
        Post eventAnnouncement = new Post("Parent-Teacher Conference", 
                                        "Annual parent-teacher conference will be held next week. Please schedule your appointments with your child's teacher.", 
                                        2, "EVENT", LocalDate.now().plusDays(7));
        eventAnnouncement.setScheduledDate(LocalDate.now().plusDays(2)); // Announce 2 days before the event
        eventAnnouncement.setPinned(true);
        
        System.out.println("   Title: " + eventAnnouncement.getTitle());
        System.out.println("   Type: " + eventAnnouncement.getPostTypeDisplay());
        System.out.println("   Category: " + eventAnnouncement.getCategoryDisplay());
        System.out.println("   Event Date: " + eventAnnouncement.getEventDate());
        System.out.println("   Scheduled to be published: " + eventAnnouncement.getScheduledDate());
        System.out.println("   Is Upcoming Event: " + eventAnnouncement.isUpcomingEvent());
        System.out.println();
        
        // Demo 4: Show different post types
        System.out.println("4. Post Type Constants:");
        System.out.println("   CLASS_ACTIVITY: " + Post.TYPE_CLASS_ACTIVITY);
        System.out.println("   SCHOOL_ANNOUNCEMENT: " + Post.TYPE_SCHOOL_ANNOUNCEMENT);
        System.out.println();
        
        // Demo 5: Show announcement categories
        System.out.println("5. Available Announcement Categories:");
        String[] categories = {"GENERAL", "EVENT", "HOLIDAY", "SCHEDULE"};
        for (String category : categories) {
            Post temp = new Post();
            temp.setCategory(category);
            System.out.println("   " + category + " -> " + temp.getCategoryDisplay());
        }
        
        System.out.println("\n=== Demo Complete ===");
        System.out.println("\nKey Features Added:");
        System.out.println("✓ Two distinct post types: Class Activities and School Announcements");
        System.out.println("✓ Category system for announcements (Event, Holiday, Schedule, General)");
        System.out.println("✓ Event dates for announcements");
        System.out.println("✓ Pin functionality for important announcements");
        System.out.println("✓ Separate UI tabs for different post types");
        System.out.println("✓ Enhanced sorting (pinned posts appear first)");
        System.out.println("✓ Type-specific form fields in the UI");
    }
}
