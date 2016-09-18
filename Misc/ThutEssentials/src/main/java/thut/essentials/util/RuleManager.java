package thut.essentials.util;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import thut.essentials.ThutEssentials;

public class RuleManager
{
    final static Field                       rulesField;
    final static Map<String, TextFormatting> charCodeMap = Maps.newHashMap();

    static
    {
        Field temp = null;
        try
        {
            temp = ConfigManager.class.getDeclaredField("rules");
        }
        catch (SecurityException | NoSuchFieldException e)
        {
            e.printStackTrace();
        }
        rulesField = temp;
        temp = ReflectionHelper.findField(TextFormatting.class, "formattingCode", "field_96329_z", "z");
        temp.setAccessible(true);
        for (TextFormatting format : TextFormatting.values())
        {
            try
            {
                char code = temp.getChar(format);
                charCodeMap.put(code + "", format);
            }
            catch (IllegalArgumentException | IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static String format(String rule)
    {
        boolean done = false;
        int index = 0;
        index = rule.indexOf('&', index);
        while (!done && index < rule.length() && index >= 0)
        {
            try
            {
                done = !rule.contains("&");
                index = rule.indexOf('&', index);
                if (index < rule.length() - 1 && index >= 0)
                {
                    String toReplace = rule.substring(index, index + 2);
                    String num = toReplace.replace("&", "");
                    TextFormatting format = charCodeMap.get(num);
                    if (format != null) rule = rule.replaceAll(toReplace, format + "");
                    else index++;
                }
                else
                {
                    done = true;
                }
            }
            catch (Exception e)
            {
                done = true;
                e.printStackTrace();
            }
        }
        return rule;
    }

    public static void addRule(ICommandSender sender, String rule) throws Exception
    {
        List<String> rulesList = getRules();
        rule = format(rule);
        rulesList.add(rule);
        sender.addChatMessage(new TextComponentString("Added rule: " + rule));
        ConfigManager.INSTANCE.updateField(rulesField, rulesList.toArray(new String[0]));
    }

    public static void delRule(ICommandSender sender, int rule) throws Exception
    {
        List<String> rulesList = getRules();
        String r = rulesList.remove(rule);
        sender.addChatMessage(new TextComponentString("Removed rule: " + r));
        ConfigManager.INSTANCE.updateField(rulesField, rulesList.toArray(new String[0]));
    }

    public static List<String> getRules()
    {
        return Lists.newArrayList(ThutEssentials.instance.config.rules);
    }
}
