package com.github.jordannegreiros.ifood.cadastro;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.github.jordannegreiros.ifood.cadastro.dto.AdicionarPratoDTO;
import com.github.jordannegreiros.ifood.cadastro.dto.AdicionarRestauranteDTO;
import com.github.jordannegreiros.ifood.cadastro.dto.AtualizarPratoDTO;
import com.github.jordannegreiros.ifood.cadastro.dto.AtualizarRestauranteDTO;
import com.github.jordannegreiros.ifood.cadastro.dto.PratoDTO;
import com.github.jordannegreiros.ifood.cadastro.dto.PratoMapper;
import com.github.jordannegreiros.ifood.cadastro.dto.RestauranteDTO;
import com.github.jordannegreiros.ifood.cadastro.dto.RestauranteMapper;
import com.github.jordannegreiros.ifood.cadastro.infra.ConstraintViolationResponse;


@Path("/restaurantes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "restaurante")
public class RestauranteResource {
	
	@Inject
	RestauranteMapper restauranteMapper;
	
	@Inject
    PratoMapper pratoMapper;

	@GET
	public List<RestauranteDTO> listar() {
		Stream<Restaurante> restaurates = Restaurante.streamAll();
		return restaurates.map(r -> restauranteMapper.toRestauranteDTO(r)).collect(Collectors.toList());
	}
	
	@POST
	@Transactional
	@APIResponse(responseCode = "201", description = "Caso restaurante seja cadastrado com sucesso")
	@APIResponse(responseCode = "400", content = @Content(schema = @Schema(allOf = ConstraintViolationResponse.class)))
	public Response adicionar(@Valid AdicionarRestauranteDTO dto) {
		Restaurante restaurante = restauranteMapper.toRestaurante(dto);
		restaurante.persist();
		return Response.status(Status.CREATED).build();
	}
	
	@PUT
	@Path("{id}")
	@Transactional
	public void atualizar(@PathParam("id") Long id, AtualizarRestauranteDTO dto) {
		Optional<Restaurante> restauranteOp = Restaurante.findByIdOptional(id);
		if (restauranteOp.isEmpty()) {
			throw new NotFoundException();
		}
		Restaurante restaurante = restauranteOp.get();
		// referencia para ser atualizada
		restauranteMapper.toRestaurante(dto, restaurante);
		restaurante.persist();
	}
	
	@DELETE
	@Path("{id}")
	@Transactional
	public void remover(@PathParam("id") Long id) {
		Optional<Restaurante> restauranteOp = Restaurante.findByIdOptional(id);
		
		restauranteOp.ifPresentOrElse(Restaurante::delete, () -> {
			throw new NotFoundException();
		});
	}
	
	/**
	 * PRATOS 
	 */
	
	@GET
	@Path("{idRestaurante}/pratos")
	@Tag(name = "prato")
	public List<PratoDTO> buscarPratos(@PathParam("idRestaurante") Long idRestaurante) {
		
		Optional<Restaurante> restauranteOp = Restaurante.findByIdOptional(idRestaurante);
		if (restauranteOp.isEmpty()) {
			throw new NotFoundException("Restaurante não existe");
		}

		Stream<Prato> pratos = Prato.stream("restaurante", restauranteOp.get());
		return pratos.map(p -> pratoMapper.toDTO(p)).collect(Collectors.toList());
	}
	
	@POST
	@Path("{idRestaurante}/pratos")
	@Transactional
	@Tag(name = "prato")
	public Response adicionarPrato(@PathParam("idRestaurante") Long idRestaurante, AdicionarPratoDTO dto) {
		
		Optional<Restaurante> restauranteOp = Restaurante.findByIdOptional(idRestaurante);
		if (restauranteOp.isEmpty()) {
			throw new NotFoundException("Restaurante não existe");
		}
		/*
		Prato prato = new Prato();
		prato.nome = dto.nome;
		prato.descricao = dto.descricao;
		prato.preco = dto.preco;
		prato.restaurante = restauranteOp.get();
		prato.persist();
		return Response.status(Status.CREATED).build();
		*/
		Prato prato = pratoMapper.toPrato(dto);
		prato.restaurante = restauranteOp.get();
		prato.persist();
		return Response.status(Status.CREATED).build();
	}
	
	@PUT
	@Path("{idRestaurante}/pratos/{id}")
	@Transactional
	@Tag(name = "prato")
	public void atualizarPrato(@PathParam("idRestaurante") Long idRestaurante, @PathParam("id") Long id, AtualizarPratoDTO dto) {
		
		Optional<Restaurante> restauranteOp = Restaurante.findByIdOptional(idRestaurante);
		if (restauranteOp.isEmpty()) {
			throw new NotFoundException("Restaurante não existe");
		}
		
		// Id do prato vai ser único, independente do restaurante
		Optional<Prato> pratoOp = Prato.findByIdOptional(id);
		
		if (pratoOp.isEmpty()) {
			throw new NotFoundException("Prato não existe");
		}
		Prato prato = pratoOp.get();
		pratoMapper.toPrato(dto, prato);
		prato.persist();
	}
	
	@DELETE
	@Path("{idRestaurante}/pratos/{id}")
	@Transactional
	@Tag(name = "prato")
	public void deletePratos(@PathParam("idRestaurante") Long idRestaurante, @PathParam("id") Long id) {
		
		Optional<Restaurante> restauranteOp = Restaurante.findByIdOptional(idRestaurante);
		if (restauranteOp.isEmpty()) {
			throw new NotFoundException("Restaurante não existe");
		}
		
		Optional<Prato> pratoOp = Prato.findByIdOptional(id);
		pratoOp.ifPresentOrElse(Prato::delete, () -> {
			throw new NotFoundException(); 
		});
	}
}
