package net.mamoe.mirai.qqandroid.network.protocol.packet.chat

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.readBytes
import net.mamoe.mirai.event.events.NewFriendEvent
import net.mamoe.mirai.event.events.NewGroupEvent
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.network.QQAndroidClient
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.MsgSvc
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.Structmsg
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.qqandroid.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.qqandroid.utils.io.serialization.loadAs
import net.mamoe.mirai.qqandroid.utils.io.serialization.writeProtoBuf

internal class NewContact {

    internal object SystemMsgNewFriend :
        OutgoingPacketFactory<NewFriendEvent?>("ProfileService.Pb.ReqSystemMsgNew.Friend") {

        operator fun invoke(client: QQAndroidClient) = buildOutgoingUniPacket(client) {
            writeProtoBuf(
                Structmsg.ReqSystemMsgNew.serializer(),
                Structmsg.ReqSystemMsgNew(
                    checktype = 2,
                    flag = Structmsg.FlagInfo(
                        frdMsgDiscuss2ManyChat = 1,
                        frdMsgGetBusiCard = 1,
                        frdMsgNeedWaitingMsg = 1,
                        frdMsgUint32NeedAllUnreadMsg = 1,
                        grpMsgMaskInviteAutoJoin = 1
                    ),
                    friendMsgTypeFlag = 1,
                    isGetFrdRibbon = false,
                    isGetGrpRibbon = false,
                    msgNum = 20,
                    version = 1000
                )
            )
        }


        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): NewFriendEvent? {
            readBytes().loadAs(Structmsg.RspSystemMsgNew.serializer()).run {
                val struct = friendmsgs?.firstOrNull()
                return if (struct == null) null else {
                    struct.msg?.run {
                        NewFriendEvent(
                            bot,
                            struct.msgSeq,
                            msgAdditional,
                            struct.reqUin,
                            groupName,
                            reqUinNick
                        )
                    }
                }
            }
        }

        internal object Action : OutgoingPacketFactory<Nothing?>("ProfileService.Pb.ReqSystemMsgAction.Friend") {

            operator fun invoke(
                client: QQAndroidClient,
                event: NewFriendEvent,
                accept: Boolean,
                blackList: Boolean = false
            ) =
                buildOutgoingUniPacket(client) {
                    writeProtoBuf(
                        Structmsg.ReqSystemMsgAction.serializer(),
                        Structmsg.ReqSystemMsgAction(
                            actionInfo = Structmsg.SystemMsgActionInfo(
                                type = if (accept) 2 else 3,
                                addFrdSNInfo = Structmsg.AddFrdSNInfo(),
                                msg = "",
                                remark = "",
                                blacklist = !accept && blackList
                            ),
                            msgSeq = event.seq,
                            reqUin = event.id,
                            srcId = 6,
                            subSrcId = 7,
                            subType = 1
                        )
                    )
                }

            override suspend fun ByteReadPacket.decode(bot: QQAndroidBot) = null
        }
    }


    internal object SystemMsgNewGroup :
        OutgoingPacketFactory<NewGroupEvent?>("ProfileService.Pb.ReqSystemMsgNew.Group") {

        operator fun invoke(client: QQAndroidClient) = buildOutgoingUniPacket(client) {
            writeProtoBuf(
                Structmsg.ReqSystemMsgNew.serializer(),
                Structmsg.ReqSystemMsgNew(
                    checktype = 3,
                    flag = Structmsg.FlagInfo(
                        frdMsgDiscuss2ManyChat = 1,
                        frdMsgGetBusiCard = 0,
                        frdMsgNeedWaitingMsg = 1,
                        frdMsgUint32NeedAllUnreadMsg = 1,
                        grpMsgGetC2cInviteJoinGroup = 1,
                        grpMsgMaskInviteAutoJoin = 1,
                        grpMsgGetDisbandedByAdmin = 1,
                        grpMsgGetOfficialAccount = 1,
                        grpMsgGetPayInGroup = 1,
                        grpMsgGetQuitPayGroupMsgFlag = 1,
                        grpMsgGetTransferGroupMsgFlag = 1,
                        grpMsgHiddenGrp = 1,
                        grpMsgKickAdmin = 1,
                        grpMsgNeedAutoAdminWording = 1,
                        grpMsgNotAllowJoinGrpInviteNotFrd = 1,
                        grpMsgSupportInviteAutoJoin = 1,
                        grpMsgWordingDown = 1
                    ),
                    friendMsgTypeFlag = 1,
                    isGetFrdRibbon = false,
                    isGetGrpRibbon = false,
                    msgNum = 5,
                    version = 1000
                )
            )
        }


        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): NewGroupEvent? {
            readBytes().loadAs(Structmsg.RspSystemMsgNew.serializer()).run {
                val struct = groupmsgs?.firstOrNull()

                return if (struct == null) null else {
                    struct.msg?.run {
                        NewGroupEvent(
                            bot,
                            struct.msgSeq,
                            msgAdditional,
                            struct.reqUin,
                            groupCode,
                            groupName,
                            reqUinNick
                        )
                    }
                }
            }
        }

        internal object Action : OutgoingPacketFactory<Nothing?>("ProfileService.Pb.ReqSystemMsgAction.Group") {

            operator fun invoke(
                client: QQAndroidClient,
                event: NewGroupEvent,
                accept: Boolean?,
                blackList: Boolean = false
            ) =
                buildOutgoingUniPacket(client) {
                    writeProtoBuf(
                        Structmsg.ReqSystemMsgAction.serializer(),
                        Structmsg.ReqSystemMsgAction(
                            actionInfo = Structmsg.SystemMsgActionInfo(
                                type = when (accept) {
                                    null -> 14 // ignore
                                    true -> 11 // accept
                                    false -> 12 // reject
                                },
                                groupCode = event.groupId,
                                msg = "",
                                remark = "",
                                blacklist = blackList
                            ),
                            groupMsgType = 1,
                            language = 1000,
                            msgSeq = event.seq,
                            reqUin = event.id,
                            srcId = 3,
                            subSrcId = 31,
                            subType = 1
                        )
                    )
                }

            override suspend fun ByteReadPacket.decode(bot: QQAndroidBot) = null
        }
    }

    internal object Del : OutgoingPacketFactory<Nothing?>("MessageSvc.PbDeleteMsg") {

        internal operator fun invoke(client: QQAndroidClient, header: MsgComm.MsgHead) = buildOutgoingUniPacket(client) {

            writeProtoBuf(
                MsgSvc.PbDeleteMsgReq.serializer(),
                MsgSvc.PbDeleteMsgReq(
                    msgItems = listOf(
                        MsgSvc.PbDeleteMsgReq.MsgItem(
                            fromUin = header.fromUin,
                            toUin = header.toUin,
                            // 群为84、好友为187。但是群通过其他方法删除，测试通过187也能删除群消息。
                            msgType = 187,
                            msgSeq = header.msgSeq,
                            msgUid = header.msgUid
                        )
                    )
                )
            )
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot) = null
    }
}
