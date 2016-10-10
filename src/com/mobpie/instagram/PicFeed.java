package com.mobpie.instagram;

public class PicFeed {
	public String fileKey; //	图片对应的filekey	string,hexical
	public int    scale;   //	图片对应的尺寸规格	int,见图片尺寸规格
	public long   authorId; //	图片作者id	int,数据库中的分配的ID
	public String authorUsername; //	图片作者的帐户名	string, 帐户名
	public String authorNick;     //	图片作者对应昵称	string, utf-8编码
	public String authorAvatar;   //	作者头像对应的filekey	string, hexical
	public long   uploadTime;     //	上传时间戳	timestamp
	public int    likeNum;        //	被赞的次数	int
	public int    commentNum;     //	评论次数	int
	public String description;    //	作者填写的描述	string, utf-8编码
	public int    status;         //	图片状态	0，正常可见；1，用户删除；2，被举报已屏蔽

	
	public String getThumbPath()
	{
		return fileKey;
	}
	
	public String getOrgPicPath()
	{
		//TODO 这里仅仅是测试用，稍候需要修改。
		return fileKey;
	}

}
