import com.draagon.meta.registry.SharedTestRegistry;
import com.draagon.meta.registry.MetaDataRegistry;

public class DebugConstraintTest {
    public static void main(String[] args) {
        // Initialize the registry like the test does
        SharedTestRegistry.getInstance();
        MetaDataRegistry registry = SharedTestRegistry.getInstance();

        // Test the specific failing call
        boolean result1 = registry.acceptsChild("field", "string", "attr", "string", "testAttr");
        System.out.println("registry.acceptsChild(\"field\", \"string\", \"attr\", \"string\", \"testAttr\") = " + result1);

        // Test the successful call from the same test
        boolean result2 = registry.acceptsChild("field", "string", "attr", "string", "pattern");
        System.out.println("registry.acceptsChild(\"field\", \"string\", \"attr\", \"string\", \"pattern\") = " + result2);

        // Test with wildcard name
        boolean result3 = registry.acceptsChild("field", "string", "attr", "string", "*");
        System.out.println("registry.acceptsChild(\"field\", \"string\", \"attr\", \"string\", \"*\") = " + result3);

        // Test with base field type
        boolean result4 = registry.acceptsChild("field", "base", "attr", "string", "testAttr");
        System.out.println("registry.acceptsChild(\"field\", \"base\", \"attr\", \"string\", \"testAttr\") = " + result4);
    }
}