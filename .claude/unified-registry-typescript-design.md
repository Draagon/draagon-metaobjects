# Unified Registry Architecture - TypeScript Implementation Design

## 🎯 **TypeScript ServiceRegistry Pattern Design**

### **Core Challenge: Module System Constraints**

TypeScript's ES module system executes code immediately on import, which fundamentally conflicts with deferred registration patterns. Our solution uses **Factory Registry Pattern** with controlled registration timing.

### **Factory Registry Architecture**

```typescript
// Core interfaces
interface ServiceRegistry {
    getServices<T>(serviceClass: Constructor<T>): T[];
    registerService<T>(serviceClass: Constructor<T>, service: T): void;
    unregisterService<T>(serviceClass: Constructor<T>, service: T): boolean;
    readonly isAvailable: boolean;
    readonly description: string;
}

interface ConstraintProvider {
    registerConstraints(registry: ConstraintRegistry): void;
    readonly description: string;
}

interface MetaDataTypeProvider {
    registerTypes(registry: MetaDataRegistry): void;
    readonly description: string;
}

type Constructor<T = {}> = new (...args: any[]) => T;
```

### **Factory-Based Registration Pattern**

```typescript
// Instead of immediate registration, use factory pattern
export interface FieldRegistration {
    readonly type: string;
    readonly subType: string;
    readonly factory: () => Constructor<MetaField>;
    readonly constraints?: ConstraintDefinition[];
}

// StringField.ts - No immediate side effects
export class StringField extends MetaField<string> {
    constructor(type: string, subType: string, name: string) {
        super(type, subType, name);
    }
    
    // Field-specific logic
    validatePattern(pattern: string): boolean {
        // Implementation
        return true;
    }
}

// Export registration factory (not immediate registration)
export const StringFieldRegistration: FieldRegistration = {
    type: 'field',
    subType: 'string',
    factory: () => StringField,
    constraints: [
        {
            type: 'placement',
            id: 'stringfield.maxlength.placement',
            description: 'String fields can have maxLength attribute',
            parentMatch: (parent) => parent instanceof StringField,
            childMatch: (child) => child instanceof IntAttribute && child.name === 'maxLength'
        },
        {
            type: 'validation',
            id: 'stringfield.pattern.validation',
            description: 'String field pattern validation',
            appliesTo: (field) => field instanceof StringField,
            validate: (field, value) => {
                const pattern = field.getAttribute('pattern');
                return pattern ? new RegExp(pattern).test(String(value)) : true;
            }
        }
    ]
};
```

### **Provider-Based Module Orchestration**

```typescript
// CoreFieldProvider.ts - Orchestrates field registrations
export class CoreFieldConstraintProvider implements ConstraintProvider {
    readonly description = 'Core field validation constraints';
    
    registerConstraints(registry: ConstraintRegistry): void {
        // Import registrations (modules execute but don't auto-register)
        const fieldRegistrations = [
            StringFieldRegistration,
            IntegerFieldRegistration,
            BooleanFieldRegistration,
            LongFieldRegistration,
            DoubleFieldRegistration
        ];
        
        // Register constraints from all field registrations
        fieldRegistrations.forEach(registration => {
            if (registration.constraints) {
                registration.constraints.forEach(constraintDef => {
                    const constraint = this.createConstraint(constraintDef);
                    registry.addConstraint(constraint);
                });
            }
        });
    }
    
    private createConstraint(def: ConstraintDefinition): Constraint {
        switch (def.type) {
            case 'placement':
                return new PlacementConstraint(def.id, def.description, def.parentMatch, def.childMatch);
            case 'validation':
                return new ValidationConstraint(def.id, def.description, def.appliesTo, def.validate);
            default:
                throw new Error(`Unknown constraint type: ${def.type}`);
        }
    }
}

export class CoreFieldTypeProvider implements MetaDataTypeProvider {
    readonly description = 'Core field type registrations';
    
    registerTypes(registry: MetaDataRegistry): void {
        const fieldRegistrations = [
            StringFieldRegistration,
            IntegerFieldRegistration,
            BooleanFieldRegistration,
            LongFieldRegistration,
            DoubleFieldRegistration
        ];
        
        fieldRegistrations.forEach(registration => {
            registry.register(
                registration.type,
                registration.subType,
                registration.factory()
            );
        });
    }
}
```

### **Registry Implementation**

```typescript
// StandardServiceRegistry.ts - TypeScript ServiceLoader equivalent
export class StandardServiceRegistry implements ServiceRegistry {
    private readonly services = new Map<Constructor<any>, Set<any>>();
    private closed = false;
    
    get isAvailable(): boolean {
        return !this.closed;
    }
    
    get description(): string {
        return 'TypeScript Standard Service Registry';
    }
    
    getServices<T>(serviceClass: Constructor<T>): T[] {
        if (this.closed) {
            throw new Error('ServiceRegistry has been closed');
        }
        
        const services = this.services.get(serviceClass);
        return services ? Array.from(services) as T[] : [];
    }
    
    registerService<T>(serviceClass: Constructor<T>, service: T): void {
        if (this.closed) {
            throw new Error('ServiceRegistry has been closed');
        }
        
        if (!(service instanceof serviceClass)) {
            throw new Error(`Service ${service.constructor.name} does not implement ${serviceClass.name}`);
        }
        
        if (!this.services.has(serviceClass)) {
            this.services.set(serviceClass, new Set());
        }
        this.services.get(serviceClass)!.add(service);
    }
    
    unregisterService<T>(serviceClass: Constructor<T>, service: T): boolean {
        if (this.closed) return false;
        
        const services = this.services.get(serviceClass);
        if (services) {
            const removed = services.delete(service);
            if (services.size === 0) {
                this.services.delete(serviceClass);
            }
            return removed;
        }
        return false;
    }
    
    close(): void {
        this.services.clear();
        this.closed = true;
    }
}
```

### **MetaDataRegistry TypeScript Implementation**

```typescript
// MetaDataRegistry.ts
export class MetaDataRegistry {
    private readonly serviceRegistry: ServiceRegistry;
    private readonly typeDefinitions = new Map<string, Constructor<MetaData>>();
    private readonly globalRequirements = new Map<string, ChildRequirement[]>();
    private initialized = false;
    
    constructor(serviceRegistry?: ServiceRegistry) {
        this.serviceRegistry = serviceRegistry || new StandardServiceRegistry();
        this.loadTypeProviders();
    }
    
    private loadTypeProviders(): void {
        if (this.initialized) return;
        
        const providers = this.serviceRegistry.getServices(MetaDataTypeProvider);
        providers.forEach(provider => {
            provider.registerTypes(this);
        });
        
        this.initialized = true;
    }
    
    register(type: string, subType: string, implementationClass: Constructor<MetaData>): void {
        const typeId = `${type}.${subType}`;
        
        const existing = this.typeDefinitions.get(typeId);
        if (existing && existing !== implementationClass) {
            throw new Error(
                `Type already registered with different implementation: ${typeId}. ` +
                `Existing: ${existing.name}, New: ${implementationClass.name}`
            );
        }
        
        this.typeDefinitions.set(typeId, implementationClass);
    }
    
    createInstance<T extends MetaData>(type: string, subType: string, name: string): T {
        const typeId = `${type}.${subType}`;
        const implementationClass = this.typeDefinitions.get(typeId);
        
        if (!implementationClass) {
            throw new Error(
                `No type registered for: ${typeId}. ` +
                `Available types: ${Array.from(this.typeDefinitions.keys()).join(', ')}`
            );
        }
        
        return new implementationClass(type, subType, name) as T;
    }
    
    getRegisteredTypes(): string[] {
        return Array.from(this.typeDefinitions.keys());
    }
}
```

### **Application Bootstrap Pattern**

```typescript
// ApplicationBootstrap.ts - Controlled initialization
export class MetaDataBootstrap {
    private static instance: MetaDataBootstrap | null = null;
    private readonly serviceRegistry: ServiceRegistry;
    private readonly metaDataRegistry: MetaDataRegistry;
    private readonly constraintRegistry: ConstraintRegistry;
    
    private constructor() {
        this.serviceRegistry = new StandardServiceRegistry();
        
        // Register providers
        this.registerProviders();
        
        // Initialize registries (triggers provider loading)
        this.metaDataRegistry = new MetaDataRegistry(this.serviceRegistry);
        this.constraintRegistry = new ConstraintRegistry(this.serviceRegistry);
    }
    
    static getInstance(): MetaDataBootstrap {
        if (!MetaDataBootstrap.instance) {
            MetaDataBootstrap.instance = new MetaDataBootstrap();
        }
        return MetaDataBootstrap.instance;
    }
    
    private registerProviders(): void {
        // Type providers
        this.serviceRegistry.registerService(MetaDataTypeProvider, new CoreFieldTypeProvider());
        this.serviceRegistry.registerService(MetaDataTypeProvider, new CoreObjectTypeProvider());
        this.serviceRegistry.registerService(MetaDataTypeProvider, new CoreAttributeTypeProvider());
        
        // Constraint providers
        this.serviceRegistry.registerService(ConstraintProvider, new CoreFieldConstraintProvider());
        this.serviceRegistry.registerService(ConstraintProvider, new CoreObjectConstraintProvider());
        this.serviceRegistry.registerService(ConstraintProvider, new CoreValidatorConstraintProvider());
    }
    
    getMetaDataRegistry(): MetaDataRegistry {
        return this.metaDataRegistry;
    }
    
    getConstraintRegistry(): ConstraintRegistry {
        return this.constraintRegistry;
    }
    
    // Force initialization (call from main application)
    static initialize(): void {
        MetaDataBootstrap.getInstance();
    }
}

// main.ts - Application entry point
import { MetaDataBootstrap } from './MetaDataBootstrap';

// Initialize MetaData system
MetaDataBootstrap.initialize();

// Now registries are available
const metaDataRegistry = MetaDataBootstrap.getInstance().getMetaDataRegistry();
const constraintRegistry = MetaDataBootstrap.getInstance().getConstraintRegistry();

// Use registries...
const userField = metaDataRegistry.createInstance<StringField>('field', 'string', 'username');
```

### **Plugin Architecture**

```typescript
// Plugin interface
export interface MetaDataPlugin {
    readonly name: string;
    readonly version: string;
    initialize(serviceRegistry: ServiceRegistry): void;
}

// Plugin implementation
export class DatabasePlugin implements MetaDataPlugin {
    readonly name = 'Database MetaData Plugin';
    readonly version = '1.0.0';
    
    initialize(serviceRegistry: ServiceRegistry): void {
        serviceRegistry.registerService(ConstraintProvider, new DatabaseConstraintProvider());
        serviceRegistry.registerService(MetaDataTypeProvider, new DatabaseTypeProvider());
    }
}

// Plugin loading
export class PluginManager {
    static async loadPlugins(serviceRegistry: ServiceRegistry, pluginPaths: string[]): Promise<void> {
        for (const pluginPath of pluginPaths) {
            try {
                const pluginModule = await import(pluginPath);
                const plugin: MetaDataPlugin = new pluginModule.default();
                plugin.initialize(serviceRegistry);
            } catch (error) {
                console.error(`Failed to load plugin ${pluginPath}:`, error);
            }
        }
    }
}
```

### **Testing Strategy**

```typescript
// Test utilities
export class TestServiceRegistry extends StandardServiceRegistry {
    constructor() {
        super();
    }
    
    reset(): void {
        this.services.clear();
    }
}

// Test setup
describe('MetaDataRegistry', () => {
    let serviceRegistry: TestServiceRegistry;
    let metaDataRegistry: MetaDataRegistry;
    let constraintRegistry: ConstraintRegistry;
    
    beforeEach(() => {
        serviceRegistry = new TestServiceRegistry();
        
        // Register test providers
        serviceRegistry.registerService(MetaDataTypeProvider, new CoreFieldTypeProvider());
        serviceRegistry.registerService(ConstraintProvider, new CoreFieldConstraintProvider());
        
        metaDataRegistry = new MetaDataRegistry(serviceRegistry);
        constraintRegistry = new ConstraintRegistry(serviceRegistry);
    });
    
    afterEach(() => {
        serviceRegistry.reset();
    });
    
    it('should create StringField when type is registered', () => {
        const field = metaDataRegistry.createInstance<StringField>('field', 'string', 'testField');
        expect(field).toBeInstanceOf(StringField);
        expect(field.name).toBe('testField');
    });
    
    it('should throw error for unregistered type', () => {
        expect(() => {
            metaDataRegistry.createInstance('field', 'unknown', 'test');
        }).toThrow('No type registered for: field.unknown');
    });
});
```

### **Webpack/Build Configuration**

```typescript
// webpack.config.js - Ensure modules aren't tree-shaken
module.exports = {
    // ...
    optimization: {
        usedExports: false, // Disable tree-shaking for provider modules
        sideEffects: false  // Mark provider modules as having side effects
    },
    // ...
};

// package.json
{
    "sideEffects": [
        "./src/providers/**/*.ts",
        "./src/types/**/*.ts"
    ]
}
```

### **TypeScript Advantages**

1. **Type Safety**: Compile-time checking for service interfaces
2. **Module System**: Clean import/export for provider orchestration
3. **Factory Pattern**: Deferred instantiation without immediate side effects
4. **Testing**: Easy mocking and service replacement
5. **Build Integration**: Webpack/Rollup can optimize provider loading

### **Key Differences from Java**

- **No Static Blocks**: Use factory registrations instead
- **Controlled Timing**: Bootstrap class manages initialization order
- **Module Orchestration**: Providers import and register factories
- **No ServiceLoader**: Manual service registration in bootstrap
- **Type Safety**: TypeScript generics provide compile-time safety

This TypeScript design provides a clean, type-safe approach that works with ES modules while maintaining the same architectural patterns as the Java OSGi-compatible approach.