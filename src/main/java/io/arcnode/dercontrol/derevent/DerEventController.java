package io.arcnode.dercontrol.derevent;

import io.arcnode.dercontrol.derevent.dto.DerControlRequest;
import io.arcnode.dercontrol.derevent.dto.DerEventResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST endpoints for utility/aggregator DERControl ingest. Thin — all logic lives in {@link
 * DerEventService}.
 */
@Tag(name = "der-events")
@RestController
@RequestMapping("/der-events")
public class DerEventController {

  private final DerEventService service;

  public DerEventController(DerEventService service) {
    this.service = service;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public DerEventResponse ingest(@Valid @RequestBody DerControlRequest request) {
    return service.ingest(request);
  }

  @GetMapping("/{mrid}")
  public DerEventResponse findOne(@PathVariable String mrid) {
    return service.findByMrid(mrid).orElseThrow(DerEventController::notFound);
  }

  @GetMapping
  public List<DerEventResponse> findByStatus(@RequestParam DerControlStatus status) {
    return service.findByStatus(status);
  }

  private static ResponseStatusException notFound() {
    return new ResponseStatusException(HttpStatus.NOT_FOUND);
  }
}
