package weiweiwang.github.search;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import weiweiwang.github.search.utils.PinyinConverter;

import java.util.HashMap;
import java.util.Map;

public class SearchService extends AbstractSearchService {
	public static final String TAG = SearchService.class.getName();
	private static final String INDEX_DIR="idx";
	private static final int REBUILD_TRIGGER_THRESHOLD=50;

	private static AbstractSearchService INSTANCE =null;
	private Context context;

	private SearchService(Context context) {
		super(context.getDir(INDEX_DIR, Context.MODE_PRIVATE));
		this.context = context;
		if(indexWriter.numDocs()<REBUILD_TRIGGER_THRESHOLD)
		{
			asyncRebuild(true);
		}
	}
	

	public static synchronized AbstractSearchService getInstance(Context context) {
		if (null == INSTANCE) {
			INSTANCE = new SearchService(context);
		}
		return INSTANCE;
	}
	
	
	@Override
	public void destroy() {
			super.destroy();
			INSTANCE = null;
			log(TAG, "destroy " + getClass().getSimpleName());
	}
	@Override
	public long rebuildCalllog(boolean urgent) {
		long start = System.currentTimeMillis();
		ContentResolver cr = this.context.getContentResolver();
		Cursor cur = cr.query(Calls.CONTENT_URI, new String[] { Calls._ID, // 0
				Calls.NUMBER, // 1
				Calls.DATE, // 2
				Calls.TYPE, // 3
		}, Calls.CACHED_NAME + " is NULL", null, Calls.DEFAULT_SORT_ORDER
				+ " LIMIT 200");
		// 只build最近的200条通话记录，加快速度
		long hits = 0;
		try {
			indexWriter
					.deleteDocuments(new Term(FIELD_TYPE, INDEX_TYPE_CALLLOG));
			// reuseable document and fields
			Document document = new Document();
			Field numberField = createTextField(FIELD_NUMBER, "");
			Field typeField = createStringField(FIELD_TYPE,INDEX_TYPE_CALLLOG);
			document.add(numberField);
			document.add(typeField);

			long current = System.currentTimeMillis();
			Map<String, Object[]> aggregatedCalllogs = new HashMap<String, Object[]>();
			while (cur.moveToNext()) {
				hits++;
				try {
					String number = stripNumber(cur.getString(1));
					long date = cur.getLong(2);
					int type = cur.getInt(3);
					long diff = (current - date);
					float boost = (type == Calls.OUTGOING_TYPE ? 2.0f : 1.0f)
							+ 1.0f
							/ (diff / (float) ONE_DAY_IN_MILLISECONDS + 1.0f);
					if (!aggregatedCalllogs.containsKey(number)) {
						aggregatedCalllogs.put(number, new Object[] { number,
								boost });
					}
//					log(TAG,"rebuild calllog:"+number+",boost:"+boost);
				} catch (Exception e) {
					log(TAG, e.toString());
				}
			}
			cur.close();
			for (Map.Entry<String, Object[]> entry : aggregatedCalllogs
					.entrySet()) {
				Object[] arr = entry.getValue();
				String number = (String) arr[0];
				float boost = (Float) arr[1];
				numberField.setStringValue(number);
				numberField.setBoost(boost);
				indexWriter.addDocument(document);
				if (!urgent) {
					yieldInterrupt();
				}
			}
			Map<String, String> userData = new HashMap<String, String>();
			userData.put("action", "rebuild");
			indexWriter.commit(userData);
			long end = System.currentTimeMillis();
			log(TAG, "commited " + hits
					+ " calllogs, time used:" + (end - start) + ",numDocs:"
					+ indexWriter.numDocs());
		} catch (Exception e) {
			android.util.Log.w(TAG, e.toString(), e);
		}
		return hits;
	}
	@Override
	public long rebuildContacts(boolean urgent) {
		long start = System.currentTimeMillis();
		ContentResolver cr = this.context.getContentResolver();
		// 取得电话本中开始一项的光标，必须先moveToNext()
		Cursor cur = cr
				.query(Phone.CONTENT_URI,
						new String[] {
								Phone.CONTACT_ID, // 0
								Phone.DISPLAY_NAME, // 1
								Phone.NUMBER, // 2
								Phone.STARRED, // 3
								Phone.TIMES_CONTACTED, // 4
								Phone.LAST_TIME_CONTACTED, // 5
						}, null, null, String.format(
								"%s DESC, %s DESC, %s DESC", Phone.STARRED,
								Phone.LAST_TIME_CONTACTED,
								Phone.TIMES_CONTACTED));
		long hits = 0;
		PinyinConverter pinyinConverter = PinyinConverter
				.getInstance(this.context);

		Document document = new Document();
		Field idField = createStringField(FIELD_ID,"");
        Field typeField = createStringField(FIELD_TYPE,INDEX_TYPE_CONTACT);
        Field nameField = createStringField(FIELD_NAME,"");
        Field numberField = createTextField(FIELD_NUMBER, "");
        Field pinyinField = createTextField(FIELD_PINYIN,"");
//        Field t9Field = createTextField(FIELD_T9,"");

		document.add(pinyinField);
//		document.add(t9Field);
		document.add(nameField);
		document.add(numberField);
		document.add(idField);
		document.add(typeField);

		try {
			indexWriter
					.deleteDocuments(new Term(FIELD_TYPE, INDEX_TYPE_CONTACT));
			long current = System.currentTimeMillis();
			while (cur.moveToNext()) {
				hits++;
				try {
					String id = cur.getString(0);
					String name = cur.getString(1);
					String number = stripNumber(cur.getString(2));
					boolean starred = cur.getInt(3) != 0;
					int contacted = cur.getInt(4);
					long lastContacted = cur.getLong(5);
					float boost = (starred ? 5.0f : 1.0f)
							+ contacted
							/ (1.0f + (current - lastContacted)
									/ (7.0f * ONE_DAY_IN_MILLISECONDS));
					String pinyin = pinyinConverter.convert(name, true);
//					log(TAG, "index contact:" + id + "\t" + name
//							+ "\t" + number + "\t" + pinyin + "\t" + boost
//							+ "\t" + starred + "\t" + contacted + "\t"
//							+ lastContacted);
					idField.setStringValue(id);
					numberField.setStringValue(number);
					nameField.setStringValue(name);
//					t9Field.setStringValue(pinyin);
					pinyinField.setStringValue(pinyin);

					pinyinField.setBoost(boost);
//					t9Field.setBoost(boost);
					numberField.setBoost(boost);
					indexWriter.addDocument(document);
					if (!urgent) {
						yieldInterrupt();
					}
				} catch (Exception e) {
					log(TAG, e.toString());
				}
			}
			cur.close();
			Map<String, String> userData = new HashMap<String, String>();
			userData.put("action", "rebuild-contacts");
			userData.put("time", String.valueOf(System.currentTimeMillis()));
			indexWriter.commit(userData);
			long end = System.currentTimeMillis();
			log(TAG, "commited " + hits
					+ " contacts, time used:" + (end - start) + ",numDocs:"
					+ indexWriter.numDocs());
		} catch (Exception e) {
			android.util.Log.w(TAG, e.toString(), e);
		}
		return hits;
	}

	@Override
	protected void log(String tag, String msg) {
		android.util.Log.d(tag,msg);
		
	}
}
