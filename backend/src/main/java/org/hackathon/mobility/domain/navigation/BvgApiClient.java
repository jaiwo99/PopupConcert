package org.hackathon.mobility.domain.navigation;

import java.util.ArrayList;

import org.hackathon.mobility.domain.gigs.model.RoutingStep;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class BvgApiClient {

    private ObjectMapper mapper = new ObjectMapper();

    public GigRoute fetchRoute(String bvgIdDestination, Double latStart, Double lonStart) {

        return new RestTemplate().execute(
            "http://demo.hafas.de/openapi/vbb-proxy/trip?originWalk=1,0,1000&lang=en&originCoordLat={latStart}&originCoordLong={lonStart}&format=json&accessId=BVG-VBB-Dezember&destExtId={bvgDest}"
            , HttpMethod.GET, clientHttpRequest -> {
            }, clientHttpResponse -> {
                final ArrayList<RoutingStep> resRoutingSteps = new ArrayList<>();
                final GigRoute res = new GigRoute(null, resRoutingSteps);

                final JsonNode json = mapper.readTree(clientHttpResponse.getBody());
                final JsonNode routingSteps = json.get("Trip").get(0).get("LegList").get("Leg");
                routingSteps.elements().forEachRemaining( step -> {
                    final JsonNode destStep = step.get("Destination");
                    final JsonNode product = step.get("Product");
                    RoutingStep.Line line = null;
                    if (product != null) {
                        line = new RoutingStep.Line(product.get("name").asText(), step.get("direction").asText(""));
                    }
                    resRoutingSteps.add(new RoutingStep(
                        destStep.get("name").asText(""),
                        line,
                        destStep.get("lat").asDouble(0d),
                        destStep.get("lon").asDouble(0d),
                        RoutingStep.RoutingType.of(destStep.get("type").asText("")),
                        destStep.get("extId").asText(null)));

                });
                return res;
            }
            , latStart, lonStart, bvgIdDestination
        );
    }
}
