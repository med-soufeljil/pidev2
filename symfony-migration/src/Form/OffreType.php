<?php

namespace App\Form;

use App\Entity\Offre;
use App\Entity\TypeOffre;
use Symfony\Bridge\Doctrine\Form\Type\EnumType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\IntegerType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class OffreType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('nomOffre', TextType::class)
            ->add('type', EnumType::class, ['class' => TypeOffre::class])
            ->add('competences', TextareaType::class)
            ->add('salaire', IntegerType::class);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults(['data_class' => Offre::class]);
    }
}
