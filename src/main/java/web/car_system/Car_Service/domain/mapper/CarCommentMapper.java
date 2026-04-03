package web.car_system.Car_Service.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import web.car_system.Car_Service.domain.dto.car_comment.CarCommentDto;
import web.car_system.Car_Service.domain.entity.CarComment;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CarCommentMapper {

    @Mapping(target = "carId", source = "car.carId")
    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "userName", source = "user.fullName")
    @Mapping(target = "userAvatar", source = "user.picture")
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "replyToUserId", source = "replyToUserId")
    @Mapping(target = "replyToUserName", source = "replyToUserName")
    @Mapping(target = "likedByCurrentUser", ignore = true)
    @Mapping(target = "replies", ignore = true)
    CarCommentDto toDto(CarComment comment);
}
