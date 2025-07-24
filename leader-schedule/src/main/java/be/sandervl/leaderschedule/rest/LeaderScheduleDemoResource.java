package be.sandervl.leaderschedule.rest;

import be.sandervl.leaderschedule.domain.LeaderScheduleSolution;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Tag(name = "Demo data", description = "Timefold-provided demo task assigning data.")
@Path("demo-data")
public class LeaderScheduleDemoResource {

    private final DemoDataGenerator dataGenerator;

    @Inject
    public LeaderScheduleDemoResource(DemoDataGenerator dataGenerator) {
        this.dataGenerator = dataGenerator;
    }

    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Unsolved demo schedule.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = LeaderScheduleSolution.class))) })
    @Operation(summary = "Find an unsolved demo schedule by ID.")
    @GET
    public Response generate() {
        return Response.ok(dataGenerator.generateDemoData()).build();
    }

}
