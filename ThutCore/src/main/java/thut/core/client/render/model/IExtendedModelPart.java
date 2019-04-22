package thut.core.client.render.model;

import java.util.HashMap;

import thut.api.maths.Vector3;
import thut.api.maths.Vector4;

public interface IExtendedModelPart extends IModelCustom
{
    void addChild(IExtendedModelPart child);

    Vector4 getDefaultRotations();

    Vector3 getDefaultTranslations();

    String getName();

    IExtendedModelPart getParent();

    int[] getRGBAB();

    HashMap<String, IExtendedModelPart> getSubParts();

    String getType();

    void resetToInit();

    void setParent(IExtendedModelPart parent);

    void setPostRotations(Vector4 rotations);

    void setPostRotations2(Vector4 rotations);

    void setPostTranslations(Vector3 translations);

    void setPreRotations(Vector4 rotations);

    void setPreTranslations(Vector3 translations);

    void setPreScale(Vector3 scale);

    default void setHidden(boolean hidden)
    {

    }

    void setRGBAB(int[] arrays);
}
