package thut.api.items;

import thut.api.ThutCore;
import net.minecraft.item.Item;

/**
 * Totally not stolen from EE3
 * @author Pahimar
 */
public class ItemThut extends Item {

  public String MOD_ID = ThutCore.MOD_ID;

  public ItemThut(String id) {
    super();
    MOD_ID = id;
//    this.setCreativeTab(ThutCore.tabThut);
  }

}
