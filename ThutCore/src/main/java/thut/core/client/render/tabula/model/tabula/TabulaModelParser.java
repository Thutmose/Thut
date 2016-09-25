package thut.core.client.render.tabula.model.tabula;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.google.common.collect.Maps;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thut.core.client.render.tabula.components.ModelJson;
import thut.core.client.render.tabula.json.JsonFactory;
import thut.core.client.render.tabula.model.IModelParser;

public class TabulaModelParser implements IModelParser<TabulaModel>
{
    @SideOnly(Side.CLIENT)
    public Map<TabulaModel, ModelJson> modelMap;

    @Override
    public TabulaModel decode(ByteBuf buf)
    {
        return null; // todo
    }

    @Override
    public void encode(ByteBuf buf, TabulaModel model)
    {
        // todo
    }

    @Override
    public String getExtension()
    {
        return "tbl";
    }

    @Override
    public Class<TabulaModel> getModelClass()
    {
        return TabulaModel.class;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public TabulaModel parse(String json) throws IOException
    {
        if (modelMap == null)
        {
            modelMap = Maps.newHashMap();
        }
        TabulaModel tabulaModel = null;
        InputStream in = IOUtils.toInputStream(json, "UTF-8");
        InputStreamReader reader = new InputStreamReader(in);
        tabulaModel = JsonFactory.getGson().fromJson(reader, TabulaModel.class);
        reader.close();
        if (tabulaModel != null)
        {
            modelMap.put(tabulaModel, new ModelJson(tabulaModel));
            return tabulaModel;
        }
        new NullPointerException("Cannot load model").printStackTrace();
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render(TabulaModel model, Entity entity)
    {
        modelMap.get(model).render(entity, 0f, 0f, 0f, 0f, 0f, 0.0625f);
    }
}
