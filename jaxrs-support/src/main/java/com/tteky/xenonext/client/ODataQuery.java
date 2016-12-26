package com.tteky.xenonext.client;

import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.ServiceHost;
import com.vmware.xenon.common.Utils;
import com.vmware.xenon.services.common.ODataQueryService;
import com.vmware.xenon.services.common.QueryTask;

import javax.validation.constraints.NotNull;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.vmware.xenon.common.UriUtils.URI_PARAM_ODATA_FILTER;

/**
 * Created by mageshwaranr
 * <p>
 * Supports query which produces homogeneous results alone
 */
public class ODataQuery {

    private ServiceHost requestSender;
    private String filterCriteria, baseUri;

    public static final String AND = " and ", EQ = " eq ", NE = " ne ";
    public static final String GT = " gt ", GE = " ge ", lt = " lt ";
    public static final String LE = " le ", OR = " or ", ANY = " any ", ALL = " all ";

    private static final String URI_SUFFIX = ODataQueryService.SELF_LINK + "?";

    public static ODataQuery newInstance() {
        return new ODataQuery();
    }

    /**
     * @param svcHost Host in which OData query need to be fired
     * @return
     */
    public ODataQuery withHost(@NotNull ServiceHost svcHost) {
        this.requestSender = svcHost;
        return this;
    }

    /**
     * @param svcHost Current host
     * @param baseUri URI pointing to external ServiceHost in which OData query needs to be fired
     * @return
     */
    public ODataQuery withHost(@NotNull ServiceHost svcHost, @NotNull String baseUri) {
        this.requestSender = svcHost;
        this.baseUri = baseUri;
        return this;
    }

    public ODataQuery withFilterCriteria(@NotNull String filterCriteria) {
        try {
            this.filterCriteria = URLEncoder.encode(filterCriteria, "UTF-8")
                    .replaceAll("\\+", "%20")
                    .replaceAll("\\%21", "!")
                    .replaceAll("\\%27", "'")
                    .replaceAll("\\%28", "(")
                    .replaceAll("\\%29", ")")
                    .replaceAll("\\%7E", "~");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * @param clazz Type to which all the results needs to be mapped
     * @param <R>
     * @return
     */
    public <R> CompletableFuture<R> execute(@NotNull Class<R> clazz) {
        StringBuilder uriString = new StringBuilder();
        if (baseUri == null) {
            uriString.append(requestSender.getUri().toString());
        } else {
            uriString.append(baseUri);
        }
        uriString.append(URI_SUFFIX);
        if (filterCriteria != null) {
            uriString.append("&")
                    .append(URI_PARAM_ODATA_FILTER)
                    .append("=")
                    .append(filterCriteria);
        }
        CompletableFuture<R> result = new CompletableFuture<>();
        try {
            Operation.createGet(new URI(uriString.toString()))
                    .setCompletion(((completedOp, failure) -> {
                        if (failure == null) {
                            QueryTask task = completedOp.getBody(QueryTask.class);
                            Map<String, Object> documents = task.results.documents;
                            result.complete(Utils.fromJson(documents.values(), clazz));
                        } else {
                            result.completeExceptionally(failure);
                        }
                    })).setReferer(requestSender.getUri())
                    .sendWith(requestSender);
        } catch (URISyntaxException e) {
            result.completeExceptionally(e);
        }
        return result;
    }


}
