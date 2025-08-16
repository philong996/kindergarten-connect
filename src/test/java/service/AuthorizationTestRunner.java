package service;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * Simple test runner for the Authorization System tests
 * Can be run as a Java application to execute tests
 */
public class AuthorizationTestRunner {
    
    public static void main(String[] args) {
        System.out.println("=== Running Authorization System Tests ===\n");
        
        // Create launcher
        Launcher launcher = LauncherFactory.create();
        
        // Create summary listener
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        
        // Build discovery request
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(AuthorizationServiceTest.class))
                .build();
        
        // Execute tests
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);
        
        // Print summary
        TestExecutionSummary summary = listener.getSummary();
        System.out.println("\n=== Test Execution Summary ===");
        System.out.println("Tests found: " + summary.getTestsFoundCount());
        System.out.println("Tests started: " + summary.getTestsStartedCount());
        System.out.println("Tests successful: " + summary.getTestsSucceededCount());
        System.out.println("Tests failed: " + summary.getTestsFailedCount());
        System.out.println("Tests skipped: " + summary.getTestsSkippedCount());
        
        if (summary.getTestsFailedCount() > 0) {
            System.out.println("\n=== Test Failures ===");
            summary.getFailures().forEach(failure -> {
                System.out.println("❌ " + failure.getTestIdentifier().getDisplayName());
                System.out.println("   Exception: " + failure.getException().getMessage());
            });
        }
        
        if (summary.getTestsSucceededCount() == summary.getTestsFoundCount()) {
            System.out.println("\n✅ All tests passed!");
        } else {
            System.out.println("\n❌ Some tests failed!");
        }
    }
}
