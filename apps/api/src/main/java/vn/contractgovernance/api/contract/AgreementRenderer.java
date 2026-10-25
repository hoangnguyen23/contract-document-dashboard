package vn.contractgovernance.api.contract;
import org.springframework.stereotype.Component;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.cos.*;
import tools.jackson.databind.JsonNode;
import vn.contractgovernance.api.shared.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
@Component
public class AgreementRenderer {
 public static final String VERSION="CG-PDF-BASE-VA-1.0";
 private final JsonSupport json;
 public AgreementRenderer(JsonSupport json){this.json=json; manifest();}
 private byte[] asset(String name){try(var in=getClass().getResourceAsStream("/agreement/"+name)){if(in==null)throw new IllegalStateException("Missing agreement asset");return in.readAllBytes();}catch(IOException e){throw new IllegalStateException("Unreadable agreement asset",e);}}
 public JsonNode manifest(){var m=json.object().put("renderer",VERSION);for(String n:List.of("BASE-VA-1.txt","TERMS-VA-1.txt","NotoSans-Regular.ttf","NotoSans-Bold.ttf","OFL.txt"))m.put(n,JsonSupport.sha256(asset(n)));if(!m.path("NotoSans-Regular.ttf").asString().equals("b85c38ecea8a7cfb39c24e395a4007474fa5a4fc864f6ee33309eb4948d232d5")||!m.path("NotoSans-Bold.ttf").asString().equals("c976e4b1b99edc88775377fcc21692ca4bfa46b6d6ca6522bfda505b28ff9d6a")||!m.path("OFL.txt").asString().equals("0dab92d0544f7b233403f14b84a663bdbfa746982eda629e7f4f9ffe1b036feb"))throw new IllegalStateException("Agreement font integrity failure");return m;}
 public byte[] render(JsonNode source)throws IOException{
  if(!manifest().equals(source.path("manifest")))throw new IOException("Renderer source manifest changed");
  var lines=new ArrayList<String>();if(source.hasNonNull("noticeId")){lines.add("THÔNG BÁO — bản nguồn đã chốt, chưa chứng minh người nhận đã đọc hoặc đồng ý.");append(lines,"Thông báo",source.path("notice"));append(lines,"Nguồn",source.path("metadata"));append(lines,"File đính kèm đã chốt",source.path("attachments"));}else for(String n:List.of("BASE-VA-1.txt","TERMS-VA-1.txt"))lines.addAll(Arrays.asList(new String(asset(n),StandardCharsets.UTF_8).split("\n")));
  if(!source.hasNonNull("noticeId")){if(source.hasNonNull("changeId")) lines.add("PHỤ LỤC THAY ĐỔI HỢP ĐỒNG — nguồn Change đã duyệt, không thay thế bản gốc.");
  lines.add("Nguồn đã phê duyệt — chỉ dữ liệu chốt, không đọc dữ liệu hiện tại:");
  append(lines,source.hasNonNull("changeId")?"Thay đổi":"Hồ sơ",source.path(source.hasNonNull("changeId")?"changeId":"dossierId"));append(lines,"Bản chốt",source.path("revision"));append(lines,"Nguồn bằng chứng và định danh",source.path("metadata"));append(lines,"Đánh giá phê duyệt",source.path("approvedChecks"));lines.add("Đại diện ngân hàng: __________________    Đại diện bên thụ hưởng: __________________");}
  try(var doc=new PDDocument();var out=new ByteArrayOutputStream()){
   var font=PDType0Font.load(doc,new ByteArrayInputStream(asset("NotoSans-Regular.ttf")),true);
   var info=new PDDocumentInformation();info.setTitle(source.hasNonNull("noticeId")?"Thông báo hợp đồng — nguồn đã chốt":source.hasNonNull("changeId")?"Phụ lục hợp đồng dịch vụ thu hộ VA — bản chưa ký":"Hợp đồng dịch vụ thu hộ VA — bản chưa ký");info.setProducer(VERSION);var cal=GregorianCalendar.from(java.time.Instant.parse(source.path("generatedAt").asString()).atZone(java.time.ZoneOffset.UTC));info.setCreationDate(cal);info.setModificationDate(cal);doc.setDocumentInformation(info);
   var ids=new COSArray();var hash=json.hash(source);ids.add(new COSString(hash));ids.add(new COSString(hash));doc.getDocument().setDocumentID(ids);
   PDPageContentStream stream=null;float y=0;
   try{for(String text:lines){StringBuilder row=new StringBuilder();for(int offset=0;offset<text.length();){int cp=text.codePointAt(offset);offset+=Character.charCount(cp);if(Character.isISOControl(cp))cp=' ';String next=new String(Character.toChars(cp));String trial=row+next;float width=font.getStringWidth(trial)*9/1000;
    if(width>490&&row.length()>0){if(stream==null||y<55){if(stream!=null)stream.close();var page=new PDPage(org.apache.pdfbox.pdmodel.common.PDRectangle.A4);doc.addPage(page);stream=new PDPageContentStream(doc,page);y=page.getMediaBox().getHeight()-50-12;}draw(stream,font,row.toString(),y);y-=14;row.setLength(0);}row.append(next);}
    if(stream==null||y<55){if(stream!=null)stream.close();var page=new PDPage(org.apache.pdfbox.pdmodel.common.PDRectangle.A4);doc.addPage(page);stream=new PDPageContentStream(doc,page);y=page.getMediaBox().getHeight()-50-12;}draw(stream,font,row.toString(),y);y-=18;
   }}finally{if(stream!=null)stream.close();}doc.save(out);return out.toByteArray();
  }
 }
 private void append(List<String> lines,String label,JsonNode value){if(value.isObject()){lines.add(label+":");value.properties().forEach(e->append(lines,"  "+e.getKey(),e.getValue()));}else if(value.isArray()){lines.add(label+":");int i=0;for(var item:value)append(lines,"  ["+(++i)+"]",item);}else lines.add(label+": "+(value.isTextual()?value.asString():json.stringify(value)));}
 private void draw(PDPageContentStream s,PDType0Font f,String text,float y)throws IOException{s.beginText();s.setFont(f,9);s.newLineAtOffset(50,y);s.showText(text);s.endText();}
}
