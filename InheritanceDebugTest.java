import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.TypeDefinition;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.simple.SimpleLoader;

public class InheritanceDebugTest {
    public static void main(String[] args) {
        System.out.println("=== INHERITANCE DEBUG TEST ===");

        // Force class loading to trigger static blocks
        try {
            Class.forName("com.draagon.meta.loader.MetaDataLoader");
            Class.forName("com.draagon.meta.loader.simple.SimpleLoader");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        MetaDataRegistry registry = MetaDataRegistry.getInstance();

        System.out.println("\n=== ALL REGISTERED TYPES ===");
        registry.getRegisteredTypes().forEach((key, typeDef) -> {
            System.out.println(String.format("Type: %s, Parent: %s, Description: %s",
                key, typeDef.getParentTypeId(), typeDef.getDescription()));
        });

        System.out.println("\n=== LOADER TYPES SPECIFICALLY ===");
        registry.getRegisteredTypes().entrySet().stream()
            .filter(entry -> entry.getKey().startsWith("loader"))
            .forEach(entry -> {
                TypeDefinition typeDef = entry.getValue();
                System.out.println(String.format("Loader Type: %s", entry.getKey()));
                System.out.println(String.format("  Parent: %s", typeDef.getParentTypeId()));
                System.out.println(String.format("  Has Parent: %s", typeDef.hasParent()));
                System.out.println(String.format("  Accepts Children Count: %d", typeDef.getAcceptsChildren().size()));
                typeDef.getAcceptsChildren().forEach(child ->
                    System.out.println(String.format("    Accepts: %s:%s named '%s'",
                        child.getChildType(), child.getChildSubType(), child.getChildName()))
                );
            });

        System.out.println("\n=== METADATA.BASE TYPE ===");
        TypeDefinition metadataBase = registry.getRegisteredTypes().get("metadata.base");
        if (metadataBase != null) {
            System.out.println("metadata.base found:");
            System.out.println(String.format("  Description: %s", metadataBase.getDescription()));
            System.out.println(String.format("  Accepts Children Count: %d", metadataBase.getAcceptsChildren().size()));
            metadataBase.getAcceptsChildren().forEach(child ->
                System.out.println(String.format("    Accepts: %s:%s named '%s'",
                    child.getChildType(), child.getChildSubType(), child.getChildName()))
            );
        } else {
            System.out.println("metadata.base NOT FOUND!");
        }
    }
}