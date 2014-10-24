import java.util.LinkedList;


public class ArcTager {
	public static void tag(LibClassifier model, Sentence st) {
		for(int i=0;i<st.getWdList().size();i++) {
			if(st.getWdList().get(i).getHead()!=-1) {
				st.getWdList().get(i).setTag(model.findBest(new TagFeature(
						st.getWdList().get(st.getWdList().get(i).getHead()).getForm(), st.getWdList().get(st.getWdList().get(i).getHead()).getPos(), 
						st.getWdList().get(i).getForm(), st.getWdList().get(i).getPos())));
			}
		}
	}
	
	public static void buildTagFeature(Sentence st, LinkedList<TagFeature> tfl) {
		for(int i=0;i<st.getWdList().size();i++) {
			if(st.getWdList().get(i).getHead()!=-1) {
				tfl.add(new TagFeature(st.getWdList().get(i).getTag(), 
						st.getWdList().get(st.getWdList().get(i).getHead()).getForm(), st.getWdList().get(st.getWdList().get(i).getHead()).getPos(), 
						st.getWdList().get(i).getForm(), st.getWdList().get(i).getPos()));
			}
		}
	}
}
