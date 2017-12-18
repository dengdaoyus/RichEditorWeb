package com.lu.richtexteditorlib;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.graphics.ColorUtils;
import android.util.AttributeSet;
import android.util.Log;

import com.lu.lubottommenu.LuBottomMenu;
import com.lu.lubottommenu.api.IBottomMenuItem;
import com.lu.lubottommenu.api.ITheme;
import com.lu.lubottommenu.logiclist.MenuItem;
import com.lu.lubottommenu.menuitem.AbstractBottomMenuItem;
import com.lu.lubottommenu.theme.AbstractTheme;
import com.lu.lubottommenu.theme.DarkTheme;
import com.lu.lubottommenu.theme.LightTheme;
import com.lu.richtexteditorlib.base.RichEditor;
import com.lu.richtexteditorlib.constant.ItemIndex;
import com.lu.richtexteditorlib.factories.BaseItemFactory;
import com.lu.richtexteditorlib.factories.DefaultItemFactory;
import com.lu.richtexteditorlib.utils.SelectController;
import com.lu.richtexteditorlib.utils.Utils;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * Created by 陆正威 on 2017/9/14.
 */

public class SimpleRichEditor extends RichEditor {

    @SuppressWarnings("unused")
    public void setOnStateChangeListener(OnStateChangeListener mOnStateChangeListener) {
        this.mOnStateChangeListener = mOnStateChangeListener;
    }

    public BaseItemFactory getBaseItemFactory() {
        if(mBaseItemFactory == null)
            mBaseItemFactory = createDefaultFactory() ;
        return mBaseItemFactory;
    }

    private DefaultItemFactory createDefaultFactory(){
        return new DefaultItemFactory();
    }


    /**
     * @param baseItemFactory the bottomItem factory that will override the default factory
     * 设置新的工厂方法生产自定义的底栏 Item 项
     */
    public void setBaseItemFactory(BaseItemFactory baseItemFactory) {
        this.mBaseItemFactory = baseItemFactory;
    }


    public interface OnEditorClickListener {
        void onLinkButtonClick();

        void onInsertImageButtonClick();

        void onLinkClick(String name, String url);

        void onImageClick(Long id);
    }

    @SuppressWarnings("unused")
    public abstract static class OnEditorClickListenerImp implements OnEditorClickListener {
        @Override
        public void onImageClick(Long id) {

        }

        @Override
        public void onInsertImageButtonClick() {

        }

        @Override
        public void onLinkButtonClick() {

        }

        @Override
        public void onLinkClick(String name, String url) {

        }
    }

    private LuBottomMenu mLuBottomMenu;
    private SelectController mSelectController;
    private OnEditorClickListener mOnEditorClickListener;
    private ArrayList<Long> mFreeItems;//不受其他items点击事件影响的items
    private ItemIndex.Register mRegister;
    private OnStateChangeListener mOnStateChangeListener;
    private BaseItemFactory mBaseItemFactory;

    public SimpleRichEditor(Context context) {
        super(context);
    }

    public SimpleRichEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SimpleRichEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public void setLuBottomMenu(@NonNull LuBottomMenu mLuBottomMenu) {
        this.mLuBottomMenu = mLuBottomMenu;
        init();
        initRichTextViewListeners();
    }

    public void setOnEditorClickListener(OnEditorClickListener mOnEditorClickListener) {
        this.mOnEditorClickListener = mOnEditorClickListener;
    }

    private void init() {
        mSelectController = SelectController.createController();
        mRegister = ItemIndex.getInstance().getRegister();
        mFreeItems = new ArrayList<>();

        addImageInsert();
        addTypefaceBranch(true, true, true, true, true);
        addMoreBranch(true, true);
        addUndo();
        addRedo();

//        等效与以下
//       mLuBottomMenu.
//                addRootItem(MenuItemFactory.generateImageItem(getContext(), 0x01, R.drawable.insert_image, false)).//
//                addRootItem(MenuItemFactory.generateImageItem(getContext(), 0x02, R.drawable.a)).//
//                addRootItem(MenuItemFactory.generateImageItem(getContext(), 0x03, R.drawable.more)).//
//                addRootItem(MenuItemFactory.generateImageItem(getContext(), 0x04, R.drawable.undo, false)).
//                addRootItem(MenuItemFactory.generateImageItem(getContext(), 0x05, R.drawable.redo, false)).
//
//                addItem(0x02, MenuItemFactory.generateImageItem(getContext(), 0x06, R.drawable.bold)).
//                addItem(0x02, MenuItemFactory.generateImageItem(getContext(), 0x07, R.drawable.italic)).
//                addItem(0x02, MenuItemFactory.generateImageItem(getContext(), 0x08, R.drawable.strikethrough)).
//                addItem(0x02, MenuItemFactory.generateImageItem(getContext(), 0x09, R.drawable.blockquote)).
//                addItem(0x02, MenuItemFactory.generateImageItem(getContext(), 0x0a, R.drawable.h1)).
//                addItem(0x02, MenuItemFactory.generateImageItem(getContext(), 0x0b, R.drawable.h2)).
//                addItem(0x02, MenuItemFactory.generateImageItem(getContext(), 0x0c, R.drawable.h3)).
//                addItem(0x02, MenuItemFactory.generateImageItem(getContext(), 0x0d, R.drawable.h4)).
//                addItem(0x03, MenuItemFactory.generateImageItem(getContext(), 0x0e, R.drawable.halving_line, false)).
//                addItem(0x03, MenuItemFactory.generateImageItem(getContext(), 0x0f, R.drawable.link, false));
        //mLuBottomMenu.setOnItemClickListener(this);

        //mSelectController.addAll(0x09L, 0x0aL, 0x0bL, 0x0cL, 0x0dL);


        mSelectController.setHandler(new SelectController.StatesTransHandler() {
            @Override
            public void handleA2B(long id) {
                if (id > 0)
                    mLuBottomMenu.setItemSelected(id, true);
            }

            @Override
            public void handleB2A(long id) {
                if (id > 0)
                    mLuBottomMenu.setItemSelected(id, false);
            }
        });
    }

    private void initRichTextViewListeners() {

        setOnDecorationChangeListener(new OnStateChangeListener() {
            @Override
            public void onStateChangeListener(String text, List<Type> types) {
                onStateChange(text,types);

                for (long id :
                        mFreeItems) {
                    mLuBottomMenu.setItemSelected(id, false);
                }
                mSelectController.reset();
                for (RichEditor.Type t :
                        types) {
                    if (!mSelectController.contain(t.getTypeCode()))
                        mLuBottomMenu.setItemSelected(t.getTypeCode(), true);
                    else
                        mSelectController.changeState(t.getTypeCode());
                }

            }
        });
        setOnTextChangeListener(new RichEditor.OnTextChangeListener() {
            @Override
            public void onTextChange(String text) {
                //Log.e("onTextChange", text);
            }
        });
        setOnFocusChangeListener(new RichEditor.OnFocusChangeListener() {
            @Override
            public void onFocusChange(boolean isFocus) {
                if (!isFocus) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        mLuBottomMenu.show(200);
                    }
                } else {
                    mLuBottomMenu.hide(200);
                }

            }
        });
        setOnLinkClickListener(new RichEditor.OnLinkClickListener() {
            @Override
            public void onLinkClick(String linkName, String url) {
                showChangeLinkDialog(linkName, url);
            }
        });
        setOnImageClickListener(new RichEditor.OnImageClickListener() {
            @Override
            public void onImageClick(Long id) {
                showImageClick(id);
            }
        });

        setOnInitialLoadListener(new RichEditor.AfterInitialLoadListener() {
            @Override
            public void onAfterInitialLoad(boolean isReady) {
                if (isReady)
                    focusEditor();
            }
        });
    }

    /**
     * @param text  传入的字段
     * @param types 含有的类型
     * 自定义时添加监听以实现自定义按钮的逻辑
     */
    private void onStateChange(String text, List<Type> types) {
        if(mOnStateChangeListener != null)
            mOnStateChangeListener.onStateChangeListener(text,types);
    }

    private void showLinkDialog() {
        if (mOnEditorClickListener != null)
            mOnEditorClickListener.onLinkButtonClick();
    }

    private void showImagePicker() {
        if (mOnEditorClickListener != null)
            mOnEditorClickListener.onInsertImageButtonClick();
    }

    private void showImageClick(Long id) {
        if (mOnEditorClickListener != null)
            mOnEditorClickListener.onImageClick(id);
    }

    private void showChangeLinkDialog(String linkName, String url) {
        if (mOnEditorClickListener != null)
            mOnEditorClickListener.onLinkClick(linkName, url);
    }

    private boolean isInSelectController(long id) {
        if (mSelectController.contain(id)) {
            mSelectController.changeState(id);
            return true;
        }
        return false;
    }

    public void setTheme(int theme){
        if(theme == AbstractTheme.DARK_THEME) {
            mLuBottomMenu.setTheme(new DarkTheme());
            //do something
        }
        else if(theme == AbstractTheme.LIGHT_THEME) {
            mLuBottomMenu.setTheme(new LightTheme());
            //do something
        }
    }

    public void setTheme(final ITheme theme){
        mLuBottomMenu.setTheme(theme);

        post(new Runnable() {
            @Override
            public void run() {
                String backgroundColor = Utils.converInt2HexColor(theme.getBackGroundColors()[0]);
                //从高亮色和基础色中找出和背景明度差异大的作为字体颜色
                double backgroundLum = ColorUtils.calculateLuminance(theme.getBackGroundColors()[0]);
                double normalLum =  ColorUtils.calculateLuminance(theme.getNormalColor());
                double accentLum =  ColorUtils.calculateLuminance(theme.getAccentColor());

                int fontColorInt;
                if(Math.abs(normalLum - backgroundLum) > Math.abs(accentLum - backgroundLum))
                    fontColorInt = theme.getNormalColor();
                else
                    fontColorInt = theme.getAccentColor();

                String fontColor = Utils.converInt2HexColor(fontColorInt);
                //找出背景色和字体色的中间色作为引用块底色
                //unused
                int color = ColorUtils.blendARGB(fontColorInt,theme.getBackGroundColors()[0],0.5f);

                exec("javascript:RE.setBackgroundColor('"+backgroundColor+"');" );
                exec("javascript:RE.setFontColor('"+fontColor+"');");
            }
        });

        //do something
    }

    public SimpleRichEditor addTypefaceBranch(boolean needBold, boolean needItalic, boolean needStrikeThrough, boolean needBlockQuote, boolean needH) {
        checkNull(mLuBottomMenu);

        if (!(needBlockQuote || needBold || needH || needItalic || needStrikeThrough))
            return this;
        if (needBlockQuote) mSelectController.add(ItemIndex.BLOCK_QUOTE);
        if (needH) mSelectController.addAll(ItemIndex.H1, ItemIndex.H2, ItemIndex.H3, ItemIndex.H4);

        if (needBold) mFreeItems.add(ItemIndex.BOLD);
        if (needItalic) mFreeItems.add(ItemIndex.ITALIC);
        if (needStrikeThrough) mFreeItems.add(ItemIndex.STRIKE_THROUGH);

        mLuBottomMenu.addRootItem( getBaseItemFactory().generateItem(getContext(),ItemIndex.A))
                .addItem(ItemIndex.A, needBold ? getBaseItemFactory().generateItem(
                        getContext(),
                        ItemIndex.BOLD,
                        new IBottomMenuItem.OnBottomItemClickListener() {
                            @Override
                            public boolean onItemClick(MenuItem item, boolean isSelected) {
                                setBold();
                                Log.e("onItemClick",item.getId()+"");

                                //不拦截不在选择控制器中的元素让Menu自己控制选择显示效果
                                return isInSelectController(item.getId());
                            }
                        }) : null)
                .addItem(ItemIndex.A, needItalic ? getBaseItemFactory().generateItem(
                        getContext(),
                        ItemIndex.ITALIC,
                        new IBottomMenuItem.OnBottomItemClickListener() {
                            @Override
                            public boolean onItemClick(MenuItem item, boolean isSelected) {
                                setItalic();
                                Log.e("onItemClick",item.getId()+"");

                                return isInSelectController(item.getId());
                            }
                        }) : null)
                .addItem(ItemIndex.A, needStrikeThrough ? getBaseItemFactory().generateItem(
                        getContext(),
                        ItemIndex.STRIKE_THROUGH,
                        new IBottomMenuItem.OnBottomItemClickListener() {
                            @Override
                            public boolean onItemClick(MenuItem item, boolean isSelected) {
                                setStrikeThrough();
                                Log.e("onItemClick",item.getId()+"");

                                return isInSelectController(item.getId());
                            }
                        }) : null)
                .addItem(ItemIndex.A, needBlockQuote ? getBaseItemFactory().generateItem(
                        getContext(),
                        ItemIndex.BLOCK_QUOTE,
                        new IBottomMenuItem.OnBottomItemClickListener() {
                            @Override
                            public boolean onItemClick(MenuItem item, boolean isSelected) {
                                setBlockquote(!isSelected);
                                Log.e("onItemClick",item.getId()+"");

                                //mSelectController.changeState(ItemIndex.BLOCK_QUOTE);
                                return isInSelectController(item.getId());
                            }
                        }) : null)

                .addItem(ItemIndex.A, needH ? getBaseItemFactory().generateItem(
                        getContext(),
                        ItemIndex.H1,
                        new IBottomMenuItem.OnBottomItemClickListener() {
                            @Override
                            public boolean onItemClick(MenuItem item, boolean isSelected) {
                                setHeading(1, !isSelected);
                                Log.e("onItemClick",item.getId()+"");

                                //mSelectController.changeState(ItemIndex.H1);
                                return isInSelectController(item.getId());
                            }
                        }) : null)
                .addItem(ItemIndex.A, needH ? getBaseItemFactory().generateItem(
                        getContext(),
                        ItemIndex.H2,
                        new IBottomMenuItem.OnBottomItemClickListener() {
                            @Override
                            public boolean onItemClick(MenuItem item, boolean isSelected) {
                                setHeading(2, !isSelected);
                                //mSelectController.changeState(ItemIndex.H2);
                                return isInSelectController(item.getId());
                            }
                        }) : null)
                .addItem(ItemIndex.A, needH ? getBaseItemFactory().generateItem(
                        getContext(),
                        ItemIndex.H3,
                        new IBottomMenuItem.OnBottomItemClickListener() {
                            @Override
                            public boolean onItemClick(MenuItem item, boolean isSelected) {
                                setHeading(3, !isSelected);
                                //mSelectController.changeState(ItemIndex.H3);
                                return isInSelectController(item.getId());
                            }
                        }) : null)
                .addItem(ItemIndex.A, needH ? getBaseItemFactory().generateItem(
                        getContext(),
                        ItemIndex.H4,
                        new IBottomMenuItem.OnBottomItemClickListener() {
                            @Override
                            public boolean onItemClick(MenuItem item, boolean isSelected) {
                                setHeading(4, !isSelected);
                                //mSelectController.changeState(ItemIndex.H4);
                                return isInSelectController(item.getId());
                            }
                        }) : null);
        return this;
    }

    public SimpleRichEditor addImageInsert() {
        checkNull(mLuBottomMenu);

        mLuBottomMenu.addRootItem(getBaseItemFactory().generateItem(
                getContext(),
                ItemIndex.INSERT_IMAGE,
                new IBottomMenuItem.OnBottomItemClickListener() {
            @Override
            public boolean onItemClick(MenuItem item, boolean isSelected) {
                showImagePicker();
                return true;
            }
        }));
        return this;
    }

    public SimpleRichEditor addMoreBranch(boolean needHalvingLine, boolean needLink) {
        checkNull(mLuBottomMenu);

        if (!needHalvingLine && !needLink)
            return this;
        mLuBottomMenu.addRootItem(getBaseItemFactory().generateItem(getContext(),ItemIndex.MORE))
                .addItem(ItemIndex.MORE, needHalvingLine ? getBaseItemFactory().generateItem(
                        getContext(),
                        ItemIndex.HALVING_LINE,
                        new IBottomMenuItem.OnBottomItemClickListener() {
                            @Override
                            public boolean onItemClick(MenuItem item, boolean isSelected) {
                                insertHr();
                                Log.e("onItemClick",item.getId()+"");
                                return false;
                            }
                        }
                ) : null)
                .addItem(ItemIndex.MORE, needLink ? getBaseItemFactory().generateItem(
                        getContext(),
                        ItemIndex.LINK,
                        new IBottomMenuItem.OnBottomItemClickListener() {
                            @Override
                            public boolean onItemClick(MenuItem item, boolean isSelected) {
                                showLinkDialog();
                                Log.e("onItemClick",item.getId()+"");

                                return false;
                            }
                        }
                ) : null);
        return this;
    }

    public SimpleRichEditor addUndo() {
        checkNull(mLuBottomMenu);

        mLuBottomMenu.addRootItem(getBaseItemFactory().generateItem(
                getContext(),
                ItemIndex.UNDO,
                new IBottomMenuItem.OnBottomItemClickListener() {
            @Override
            public boolean onItemClick(MenuItem item, boolean isSelected) {
                undo();
                return false;
            }
        }));
        return this;
    }

    public SimpleRichEditor addRedo() {
        checkNull(mLuBottomMenu);

        mLuBottomMenu.addRootItem(getBaseItemFactory().generateItem(getContext(),
                ItemIndex.REDO,
                new IBottomMenuItem.OnBottomItemClickListener() {
            @Override
            public boolean onItemClick(MenuItem item, boolean isSelected) {
                redo();
                return false;
            }
        }));
        return this;
    }

    @SuppressWarnings("unused")
    public SimpleRichEditor addCustomItem(long parentId, long id, AbstractBottomMenuItem item) {
        checkNull(mLuBottomMenu);

        if (!mRegister.hasRegister(parentId)) {
            throw new RuntimeException(parentId + ":" + ItemIndex.NO_REGISTER_EXCEPTION);
        }
        if (mRegister.isDefaultId(id))
            throw new RuntimeException(id + ":" + ItemIndex.HAS_REGISTER_EXCEPTION);

        if (!mRegister.hasRegister(id))
            mRegister.register(id);

        item.getMenuItem().setId(id);
        mLuBottomMenu.addItem(parentId, item);
        return this;
    }

    public SimpleRichEditor addRootCustomItem(long id, AbstractBottomMenuItem item) {
        checkNull(mLuBottomMenu);

        if (mRegister.isDefaultId(id))
            throw new RuntimeException(id + ":" + ItemIndex.HAS_REGISTER_EXCEPTION);
        if (!mRegister.hasRegister(id))
            mRegister.register(id);
        item.getMenuItem().setId(id);
        mLuBottomMenu.addRootItem(item);
        return this;
    }

    private void checkNull(Object obj){
        if(obj == null)
            throw new RuntimeException("object can't be null");
    }
}
