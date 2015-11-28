package thut.api.block;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.minecraftforge.common.property.IUnlistedProperty;

public class PropertyInteger implements IUnlistedProperty<Integer>
{
    private final String name;
    private final Predicate<Integer> validator;

    public PropertyInteger(String name)
    {
        this(name, Predicates.<Integer>alwaysTrue());
    }

    public PropertyInteger(String name, Predicate<Integer> validator)
    {
        this.name = name;
        this.validator = validator;
    }

    public String getName()
    {
        return name;
    }

    public boolean isValid(Integer value)
    {
        return validator.apply(value);
    }

    public Class<Integer> getType()
    {
        return Integer.class;
    }

    public String valueToString(Integer value)
    {
        return value.toString();
    }
}