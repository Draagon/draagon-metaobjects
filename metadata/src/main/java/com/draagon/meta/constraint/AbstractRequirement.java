package com.draagon.meta.constraint;

/**
 * Abstract requirement for placement constraints defining whether a child must be abstract or concrete.
 * Used primarily for metadata.root constraints where most metadata types must be abstract.
 */
public enum AbstractRequirement {
    /**
     * Can be abstract or concrete (default for most cases)
     */
    ANY,
    
    /**
     * Must have isAbstract=true (default for metadata.root children except objects)
     */
    MUST_BE_ABSTRACT,
    
    /**
     * Must have isAbstract=false or no isAbstract attribute
     */
    MUST_BE_CONCRETE
}
