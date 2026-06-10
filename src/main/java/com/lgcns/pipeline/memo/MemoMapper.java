package com.lgcns.pipeline.memo;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MemoMapper {
//    MemoDTO toDto(Memo memo);

    Memo toEntity(MemoDTO dto);

    List<MemoDTO> toDTOList(List<Memo> memos);
}
