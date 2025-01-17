package com.github.jordannegreiros.ifood.cadastro.dto;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.github.jordannegreiros.ifood.cadastro.Prato;

@Mapper(componentModel = "cdi")
public interface PratoMapper {
	
	PratoDTO toDTO(Prato p);
	
	Prato toPrato(AdicionarPratoDTO dto);
	
	void toPrato(AtualizarPratoDTO dto, @MappingTarget Prato prato);
}
