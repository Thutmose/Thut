package thut.core.client.render.tabula.model.tabula;

import thut.core.client.render.tabula.json.JsonTabulaModel;
import thut.core.client.render.tabula.model.IModel;

public class TabulaModel extends JsonTabulaModel implements IModel {
    
    private String modelName;
    private String authorName;

    @Override
    public String getAuthor() {
        return authorName;
    }

    @Override
    public String getName() {
        return modelName;
    }
}
