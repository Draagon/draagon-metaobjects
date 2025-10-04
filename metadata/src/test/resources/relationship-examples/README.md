# MetaRelationship Examples

This directory contains example metadata JSON files demonstrating the MetaObjects relationship system. The relationship system is designed for AI code generation simplicity while supporting common relational database and document store patterns.

## Core Relationship Patterns

### Essential Attributes (AI specifies these 4)

1. **`@targetObject`** - The target object this relationship points to
2. **`@cardinality`** - Either "one" or "many"
3. **`@ownership`** - Either "owns" or "references"
4. **`@referencedBy`** - Field name that implements the relationship

### Derived Properties (automatic)

- **Semantic Type**: "composition" (owns) or "association" (references)
- **Lifecycle**: "dependent" (owns) or "independent" (references)

## Four Common RDB Relationship Patterns

### 1. One-to-One Composition (owns + one)
```json
{
  "relationship": {
    "name": "profile",
    "@targetObject": "UserProfile",
    "@cardinality": "one",
    "@ownership": "owns",
    "@referencedBy": "profileId"
  }
}
```
**Meaning**: Parent object owns exactly one child object. When parent is deleted, child is also deleted.
**RDB Implementation**: Foreign key in child table, cascade delete
**Examples**: User → Profile, Order → PaymentInfo

### 2. One-to-Many Composition (owns + many)
```json
{
  "relationship": {
    "name": "addresses",
    "@targetObject": "Address",
    "@cardinality": "many",
    "@ownership": "owns",
    "@referencedBy": "addressIds"
  }
}
```
**Meaning**: Parent object owns multiple child objects. When parent is deleted, all children are deleted.
**RDB Implementation**: Foreign key in child table, cascade delete
**Examples**: User → Addresses, Order → OrderItems

### 3. One-to-One Association (references + one)
```json
{
  "relationship": {
    "name": "manager",
    "@targetObject": "User",
    "@cardinality": "one",
    "@ownership": "references",
    "@referencedBy": "managerId"
  }
}
```
**Meaning**: Parent object references exactly one independent object. Lifecycle is independent.
**RDB Implementation**: Foreign key in parent table, no cascade delete
**Examples**: Employee → Manager, Order → Customer

### 4. One-to-Many Association (references + many)
```json
{
  "relationship": {
    "name": "favoriteProducts",
    "@targetObject": "Product",
    "@cardinality": "many",
    "@ownership": "references",
    "@referencedBy": "favoriteProductIds"
  }
}
```
**Meaning**: Parent object references multiple independent objects. Lifecycle is independent.
**RDB Implementation**: Junction table or array of foreign keys
**Examples**: User → FavoriteProducts, Category → Products

## Example Files

### 1. `simple-relationship-patterns.json`
Demonstrates all four core relationship patterns with clear examples and descriptions.

### 2. `ecommerce-relationships.json`
Comprehensive e-commerce domain model showing:
- User compositions (profile, addresses)
- User associations (orders, manager)
- Order compositions (orderItems)
- Order associations (addresses)
- Product associations (category)
- Self-referencing relationships (category hierarchy)

### 3. `blog-relationships.json`
Blog domain model showing:
- Author compositions (posts, profile)
- Post compositions (comments)
- Post associations (tags, category)
- Self-referencing relationships (comment replies, category hierarchy)

## AI Code Generation Benefits

### Simple Decision Tree
AI only needs to choose:
1. **Target Object** - What object does this point to?
2. **Cardinality** - One or many?
3. **Ownership** - Does parent own the child or just reference it?
4. **Field Name** - What field implements this relationship?

### Cross-Language Mapping
The semantic model maps directly to:
- **Java**: JPA annotations (@OneToOne, @OneToMany, @ManyToOne)
- **C#**: Entity Framework relationships
- **TypeScript**: Interface relationships
- **SQL**: Foreign keys and junction tables
- **MongoDB**: Embedded documents vs references

### Hidden Complexity
Complex attributes are derived automatically:
- **semanticType**: composition vs association
- **lifecycle**: dependent vs independent
- **direction**: unidirectional (default)
- **implementation**: reference (default)

## Usage in Code Generation

### JPA Generation Example
```java
// One-to-One Composition: @ownership="owns", @cardinality="one"
@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
@JoinColumn(name = "profile_id")
private UserProfile profile;

// One-to-Many Association: @ownership="references", @cardinality="many"
@ManyToMany(fetch = FetchType.LAZY)
@JoinTable(name = "user_favorite_products",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "product_id"))
private List<Product> favoriteProducts;
```

### TypeScript Interface Generation Example
```typescript
// One-to-One Composition
interface User {
  id: number;
  name: string;
  profile: UserProfile;  // Embedded object
}

// One-to-Many Association
interface User {
  id: number;
  name: string;
  favoriteProductIds: number[];  // Array of IDs
}
```

## Migration from MetaKey

The relationship system replaces the database-specific MetaKey system:

### Before (MetaKey - Database Specific)
```json
{
  "key": {
    "name": "userOrderFK",
    "subType": "foreign",
    "@foreignObjectRef": "User",
    "@foreignKey": "userId"
  }
}
```

### After (MetaRelationship - Model Driven)
```json
{
  "relationship": {
    "name": "user",
    "@targetObject": "User",
    "@cardinality": "one",
    "@ownership": "references",
    "@referencedBy": "userId"
  }
}
```

The relationship approach is more semantic and works across different persistence technologies (RDB, document stores, graph databases).