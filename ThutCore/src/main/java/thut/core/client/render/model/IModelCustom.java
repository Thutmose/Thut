package thut.core.client.render.model;

public interface IModelCustom
{
    void renderAll();

    void renderAllExcept(String... excludedGroupNames);

    void renderOnly(String... groupNames);

    void renderPart(String partName);

    default void renderAll(IModelRenderer<?> renderer)
    {
        renderAll();
    }

    default void renderAllExcept(IModelRenderer<?> renderer, String... excludedGroupNames)
    {
        renderAllExcept(excludedGroupNames);
    }

    default void renderOnly(IModelRenderer<?> renderer, String... groupNames)
    {
        renderOnly(groupNames);
    }

    default void renderPart(IModelRenderer<?> renderer, String partName)
    {
        renderPart(partName);
    }
}
