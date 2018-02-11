package thut.core.client.render.model;

public interface IModelCustom
{
    default void renderAll()
    {

    }

    default void renderAllExcept(String... excludedGroupNames)
    {

    }

    default void renderOnly(String... groupNames)
    {

    }

    default void renderPart(String partName)
    {

    }

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
