package thut.core.client.render.smd;

import java.util.HashMap;

import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.core.client.render.model.IExtendedModelPart;

/** This is an IExtendedModelPart which defaults most stuff to null or
 * non-existant, this is to be used for things which want to be rendered using
 * other wrappers, but only need the renderAll method. */
public interface IFakeExtendedPart extends IExtendedModelPart
{
    default void addChild(IExtendedModelPart child)
    {
    }

    default Vector4 getDefaultRotations()
    {
        return null;
    }

    default Vector3 getDefaultTranslations()
    {
        return null;
    }

    String getName();

    default IExtendedModelPart getParent()
    {
        return null;
    }

    default int[] getRGBAB()
    {
        return null;
    }

    HashMap<String, IExtendedModelPart> getSubParts();

    String getType();

    default void resetToInit()
    {
    }

    default void setParent(IExtendedModelPart parent)
    {
    }

    default void setPostRotations(Vector4 rotations)
    {
    }

    default void setPostRotations2(Vector4 rotations)
    {
    }

    default void setPostTranslations(Vector3 translations)
    {
    }

    default void setPreRotations(Vector4 rotations)
    {
    }

    default void setPreTranslations(Vector3 translations)
    {
    }

    default void setPreScale(Vector3 scale)
    {
    }

    default void setRGBAB(int[] arrays)
    {
    }
}
