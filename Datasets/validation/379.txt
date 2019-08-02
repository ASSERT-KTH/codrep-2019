package com.developmentontheedge.be5.server.controllers;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.databasemodel.DatabaseModel;
import com.developmentontheedge.be5.databasemodel.RecordModel;
import com.developmentontheedge.be5.server.servlet.support.ApiControllerSupport;
import com.developmentontheedge.be5.server.util.RequestUtils;
import com.developmentontheedge.be5.web.Controller;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;

import javax.inject.Inject;import javax.inject.Singleton;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Example url
 * api/download?_t_=attachments&_typeColumn_=mimeType&_charsetColumn_=mimeCharset&_filenameColumn_=name&_dataColumn_=data&_download_=yes&ID=7326
 */
@Singleton
public class DownloadController extends ApiControllerSupport implements Controller
{
    private final DatabaseModel database;

    @Inject
    public DownloadController(DatabaseModel database)
    {
        this.database = database;
    }

    @Override
    public void generate(Request req, Response res, String requestSubUrl)
    {
        String entity = req.getNonEmpty("_t_");
        Long ID = req.getLong("ID");

        String typeColumn = req.getOrDefault("_typeColumn_", "mimeType");
        String filenameColumn = req.getOrDefault("_filenameColumn_", "name");
        String dataColumn = req.getOrDefault("_dataColumn_", "data");

        String charsetColumn = req.get("_charsetColumn_");
        boolean download = "yes".equals(req.get("_download_"));

        RecordModel record = database.getEntity(entity).get(ID);
        if (record == null)
        {
            throw new RuntimeException("File not found.");
        }
        String filename = record.getValueAsString(filenameColumn);
        String contentType = record.getValueAsString(typeColumn);
        Object data = record.getValue(dataColumn);
        String charset = charsetColumn != null ? record.getValueAsString(charsetColumn) : StandardCharsets.UTF_8.name();

        InputStream in;

        if (data instanceof byte[])//postgres, mysql
        {
            in = new ByteArrayInputStream((byte[]) data);
        }
//        else if (data instanceof Blob)
//        {
//            in = ((Blob) data).getBinaryStream();
//        }
//        else if (data instanceof String)
//        {
//            in = new ByteArrayInputStream(((String) data).getBytes(charset));
//        }
        else
        {
            throw Be5Exception.internal("Unknown data type");
        }

        RequestUtils.sendFile(res, download, filename, contentType, charset, in);
    }

}
