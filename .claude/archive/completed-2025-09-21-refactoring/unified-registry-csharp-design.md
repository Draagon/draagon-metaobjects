# Unified Registry Architecture - C# (.NET) Implementation Design

## 🎯 **C# ServiceRegistry Pattern Design**

### **Core Architecture**

```csharp
// Universal service registry interface
public interface IServiceRegistry
{
    IEnumerable<T> GetServices<T>() where T : class;
    void RegisterService<T>(T service) where T : class;
    bool UnregisterService<T>(T service) where T : class;
    bool IsAvailable { get; }
    string Description { get; }
}

// .NET DI-based implementation
public class NetServiceRegistry : IServiceRegistry
{
    private readonly IServiceProvider _serviceProvider;
    
    public NetServiceRegistry(IServiceProvider serviceProvider)
    {
        _serviceProvider = serviceProvider;
    }
    
    public IEnumerable<T> GetServices<T>() where T : class
    {
        return _serviceProvider.GetServices<T>();
    }
    
    // Additional implementation...
}
```

### **ConstraintProvider Pattern**

```csharp
// C# ConstraintProvider interface
public interface IConstraintProvider
{
    void RegisterConstraints(IConstraintRegistry registry);
    string Description { get; }
}

// Implementation for field constraints
[Service] // Custom attribute for service registration
public class CoreFieldConstraintProvider : IConstraintProvider
{
    public string Description => "Core field validation constraints";
    
    public void RegisterConstraints(IConstraintRegistry registry)
    {
        // String field constraints
        registry.AddConstraint(new PlacementConstraint(
            "stringfield.maxlength.placement",
            parent => parent is StringField,
            child => child is IntAttribute && child.Name == "maxLength"
        ));
        
        // Integer field constraints
        registry.AddConstraint(new ValidationConstraint(
            "intfield.range.validation",
            field => field is IntegerField,
            (field, value) => ValidateIntegerRange(field, value)
        ));
    }
    
    private static bool ValidateIntegerRange(IMetaData field, object value)
    {
        // C# validation logic
        if (field.GetAttribute("minValue") is IntAttribute minAttr &&
            field.GetAttribute("maxValue") is IntAttribute maxAttr)
        {
            // Range validation
            return true;
        }
        return true;
    }
}
```

### **MetaDataRegistry C# Implementation**

```csharp
// C# MetaDataRegistry using .NET DI
public class MetaDataRegistry : IMetaDataRegistry
{
    private readonly IServiceRegistry _serviceRegistry;
    private readonly ConcurrentDictionary<MetaDataTypeId, TypeDefinition> _typeDefinitions;
    private readonly ConcurrentDictionary<string, List<ChildRequirement>> _globalRequirements;
    
    public MetaDataRegistry(IServiceRegistry serviceRegistry)
    {
        _serviceRegistry = serviceRegistry;
        _typeDefinitions = new ConcurrentDictionary<MetaDataTypeId, TypeDefinition>();
        _globalRequirements = new ConcurrentDictionary<string, List<ChildRequirement>>();
        LoadTypeProviders();
    }
    
    private void LoadTypeProviders()
    {
        var providers = _serviceRegistry.GetServices<IMetaDataTypeProvider>();
        foreach (var provider in providers)
        {
            provider.RegisterTypes(this);
        }
    }
    
    public T CreateInstance<T>(string type, string subType, string name) where T : class, IMetaData
    {
        var typeId = new MetaDataTypeId(type, subType);
        if (_typeDefinitions.TryGetValue(typeId, out var definition))
        {
            return (T)Activator.CreateInstance(definition.ImplementationClass, type, subType, name);
        }
        throw new MetaDataException($"No type registered for: {typeId.ToQualifiedName()}");
    }
}
```

### **Dependency Injection Setup**

```csharp
// Startup.cs configuration
public void ConfigureServices(IServiceCollection services)
{
    // Register service registry
    services.AddSingleton<IServiceRegistry, NetServiceRegistry>();
    
    // Auto-register all constraint providers
    services.Scan(scan => scan
        .FromAssembliesOf<CoreFieldConstraintProvider>()
        .AddClasses(classes => classes.AssignableTo<IConstraintProvider>())
        .AsImplementedInterfaces()
        .WithSingletonLifetime());
    
    // Auto-register all type providers
    services.Scan(scan => scan
        .FromAssembliesOf<CoreMetaDataTypeProvider>()
        .AddClasses(classes => classes.AssignableTo<IMetaDataTypeProvider>())
        .AsImplementedInterfaces()
        .WithSingletonLifetime());
    
    // Register registries
    services.AddSingleton<IMetaDataRegistry, MetaDataRegistry>();
    services.AddSingleton<IConstraintRegistry, ConstraintRegistry>();
}

// Initialization service
public class MetaDataInitializationService : IHostedService
{
    private readonly IMetaDataRegistry _metaDataRegistry;
    private readonly IConstraintRegistry _constraintRegistry;
    
    public MetaDataInitializationService(
        IMetaDataRegistry metaDataRegistry,
        IConstraintRegistry constraintRegistry)
    {
        _metaDataRegistry = metaDataRegistry;
        _constraintRegistry = constraintRegistry;
    }
    
    public Task StartAsync(CancellationToken cancellationToken)
    {
        // Registries auto-initialize via DI
        // Force any lazy initialization here if needed
        return Task.CompletedTask;
    }
    
    public Task StopAsync(CancellationToken cancellationToken)
    {
        // Cleanup if needed
        return Task.CompletedTask;
    }
}
```

### **Type Registration with Static Constructors**

```csharp
// C# field implementation with static registration
public class StringField : MetaField<string>
{
    static StringField()
    {
        // Static constructor ensures registration happens once
        RuntimeHelpers.RunClassConstructor(typeof(StringFieldTypeProvider).TypeHandle);
    }
    
    public StringField(string type, string subType, string name) 
        : base(type, subType, name)
    {
    }
}

// Separate type provider for dependency injection
[Service]
public class StringFieldTypeProvider : IMetaDataTypeProvider
{
    public void RegisterTypes(IMetaDataRegistry registry)
    {
        registry.Register(typeof(StringField), def => def
            .Type("field").SubType("string")
            .Description("String field with pattern validation"));
    }
}
```

### **C# Advantages**

1. **Native DI**: .NET Core dependency injection is first-class
2. **Assembly Scanning**: Scrutor package provides excellent auto-registration
3. **Static Constructors**: Reliable one-time initialization with `RuntimeHelpers`
4. **Hosted Services**: Perfect lifecycle management for initialization
5. **Strongly Typed**: Generics provide compile-time safety
6. **Concurrent Collections**: Built-in thread-safe collections

### **Plugin Architecture**

```csharp
// Plugin interface
public interface IMetaDataPlugin
{
    void Initialize(IServiceCollection services);
    string Name { get; }
    Version Version { get; }
}

// Plugin implementation
public class DatabasePlugin : IMetaDataPlugin
{
    public string Name => "Database MetaData Plugin";
    public Version Version => new Version(1, 0, 0);
    
    public void Initialize(IServiceCollection services)
    {
        services.AddSingleton<IConstraintProvider, DatabaseConstraintProvider>();
        services.AddSingleton<IMetaDataTypeProvider, DatabaseTypeProvider>();
    }
}

// Plugin loading
public static void LoadPlugins(IServiceCollection services)
{
    var pluginAssemblies = Directory.GetFiles("plugins", "*.dll")
        .Select(Assembly.LoadFrom);
        
    foreach (var assembly in pluginAssemblies)
    {
        var pluginTypes = assembly.GetTypes()
            .Where(t => typeof(IMetaDataPlugin).IsAssignableFrom(t) && !t.IsInterface);
            
        foreach (var pluginType in pluginTypes)
        {
            var plugin = (IMetaDataPlugin)Activator.CreateInstance(pluginType);
            plugin.Initialize(services);
        }
    }
}
```

### **Performance Considerations**

- **Singleton Registries**: Registries registered as singletons for performance
- **Concurrent Collections**: Thread-safe operations without locks
- **Lazy Initialization**: Services initialized on first access
- **Memory Management**: .NET GC handles cleanup automatically
- **Caching**: Built-in DI container caching for service instances

### **Testing Strategy**

```csharp
// Test setup
[TestClass]
public class MetaDataRegistryTests
{
    private IServiceProvider _serviceProvider;
    private IMetaDataRegistry _registry;
    
    [TestInitialize]
    public void Setup()
    {
        var services = new ServiceCollection();
        
        // Register test-specific providers
        services.AddSingleton<IServiceRegistry, TestServiceRegistry>();
        services.AddSingleton<IMetaDataRegistry, MetaDataRegistry>();
        services.AddSingleton<IConstraintProvider, TestConstraintProvider>();
        
        _serviceProvider = services.BuildServiceProvider();
        _registry = _serviceProvider.GetRequiredService<IMetaDataRegistry>();
    }
    
    [TestMethod]
    public void Should_CreateStringField_When_TypeRegistered()
    {
        // Test implementation
        var field = _registry.CreateInstance<StringField>("field", "string", "testField");
        Assert.IsNotNull(field);
        Assert.AreEqual("testField", field.Name);
    }
}
```

This C# design leverages .NET's native dependency injection and provides a clean, strongly-typed approach to the unified registry pattern that will work seamlessly with the Java OSGi-compatible approach.