package	 com.developmentontheedge.be5.base.util;

import com.developmentontheedge.be5.metadata.model.EntityItem;
import com.developmentontheedge.beans.json.JsonFactory;

import java.util.HashMap;
import java.util.Map;

public class LayoutUtils
{
    public static Map<String, Object> getLayoutObject(EntityItem entityItem)
    {
        if (!entityItem.getLayout().isEmpty())
        {
            return JsonFactory.jsonb.fromJson(entityItem.getLayout(),
                    new HashMap<String, Object>()
                    {
                    }.getClass().getGenericSuperclass());
        }
        else
        {
            return new HashMap<>();
        }
    }
}
